package com.raisedeveloper.server.domain.exercise.application;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;
import com.raisedeveloper.server.domain.user.infra.UserAlarmSettingsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmScheduleService {

	private static final String REDIS_KEY = "alarm:next_fire_at";
	private static final int MAX_LOOKAHEAD_DAYS = 7;

	private final StringRedisTemplate redisTemplate;
	private final UserAlarmSettingsRepository userAlarmSettingsRepository;

	public List<Long> fetchDueUserIds(LocalDateTime now, int limit) {
		ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
		double maxScore = toScore(now);
		Set<String> raw = zset.rangeByScore(REDIS_KEY, Double.NEGATIVE_INFINITY, maxScore, 0, limit);
		if (raw == null || raw.isEmpty()) {
			return List.of();
		}
		return raw.stream()
			.map(Long::valueOf)
			.toList();
	}

	@Transactional
	public void refreshForUserId(Long userId, LocalDateTime now) {
		UserAlarmSettings settings = userAlarmSettingsRepository.findByUserId(userId)
			.orElse(null);
		if (settings == null) {
			removeFromSchedule(userId);
			return;
		}
		refreshForSettings(settings, now);
	}

	@Transactional
	public void refreshAll(LocalDateTime now) {
		List<UserAlarmSettings> settingsList = userAlarmSettingsRepository.findAllWithUser();
		if (settingsList.isEmpty()) {
			return;
		}
		for (UserAlarmSettings settings : settingsList) {
			refreshForSettings(settings, now);
		}
	}

	@Transactional
	public void refreshForSettings(UserAlarmSettings settings, LocalDateTime now) {
		LocalDateTime nextFireAt = calculateNextFireAt(settings, now);
		applyNextFireAt(settings, nextFireAt);
	}

	@Transactional
	public void applyNextFireAt(UserAlarmSettings settings, LocalDateTime nextFireAt) {
		settings.updateNextFireAt(nextFireAt);
		userAlarmSettingsRepository.save(settings);
		if (nextFireAt == null) {
			removeFromSchedule(settings.getUser().getId());
			return;
		}
		redisTemplate.opsForZSet().add(REDIS_KEY, settings.getUser().getId().toString(), toScore(nextFireAt));
	}

	public void removeFromSchedule(Long userId) {
		redisTemplate.opsForZSet().remove(REDIS_KEY, userId.toString());
	}

	@Transactional
	public void advanceAfterDue(UserAlarmSettings settings, LocalDateTime scheduledAt, LocalDateTime now) {
		LocalDateTime nextFireAt = calculateNextFireAt(settings, now);
		applyNextFireAt(settings, nextFireAt);
	}

	public LocalDateTime calculateNextFireAt(
		UserAlarmSettings settings,
		LocalDateTime now
	) {
		if (settings == null) {
			return null;
		}
		LocalTime activeStart = settings.getActiveStartAt();
		LocalTime activeEnd = settings.getActiveEndAt();
		if (activeStart == null || activeEnd == null) {
			return null;
		}

		LocalDateTime nextFireAt = now.truncatedTo(ChronoUnit.MINUTES)
			.plusMinutes(settings.getAlarmInterval());

		if (settings.isDnd()) {
			LocalDateTime dndFinishedAt = settings.getDndFinishedAt();
			if (dndFinishedAt != null && nextFireAt.isBefore(dndFinishedAt)) {
				nextFireAt = dndFinishedAt.plusMinutes(settings.getAlarmInterval());
			}
		}

		Set<DayOfWeek> repeatDays = parseRepeatDays(settings.getRepeatDays());
		if (repeatDays.isEmpty()) {
			return null;
		}

		if (!repeatDays.contains(nextFireAt.getDayOfWeek())) {
			LocalDate nextRepeat = nextRepeatDay(nextFireAt.toLocalDate(), repeatDays);
			if (nextRepeat == null) {
				return null;
			}
			nextFireAt = LocalDateTime.of(nextRepeat, activeStart)
				.plusMinutes(settings.getAlarmInterval());
		}

		LocalTime candidate = nextFireAt.toLocalTime();
		LocalTime focusStart = settings.getFocusStartAt();
		LocalTime focusEnd = settings.getFocusEndAt();

		boolean inActive = !candidate.isBefore(activeStart) && !candidate.isAfter(activeEnd);
		boolean inFocus = focusStart != null && focusEnd != null
			&& !candidate.isBefore(focusStart) && !candidate.isAfter(focusEnd);

		if (inActive && !inFocus) {
			return nextFireAt;
		}

		if (!inActive) {
			LocalDateTime shifted = LocalDateTime.of(nextFireAt.toLocalDate(), activeStart)
				.plusMinutes(settings.getAlarmInterval());
			if (shifted.toLocalTime().isAfter(activeEnd)) {
				return moveToNextRepeatDay(nextFireAt.toLocalDate(), repeatDays, activeStart);
			}
			return shifted;
		}

		LocalTime afterFocus = focusEnd.plusMinutes(settings.getAlarmInterval());
		if (!afterFocus.isBefore(activeStart) && !afterFocus.isAfter(activeEnd)) {
			return LocalDateTime.of(nextFireAt.toLocalDate(), afterFocus);
		}

		LocalDateTime fallback = LocalDateTime.of(nextFireAt.toLocalDate(), activeStart)
			.plusMinutes(settings.getAlarmInterval());
		if (fallback.toLocalTime().isAfter(activeEnd)) {
			return moveToNextRepeatDay(nextFireAt.toLocalDate(), repeatDays, activeStart);
		}
		return fallback;
	}

	private Set<DayOfWeek> parseRepeatDays(String repeatDays) {
		if (repeatDays == null || repeatDays.isBlank()) {
			return EnumSet.noneOf(DayOfWeek.class);
		}
		String[] parts = repeatDays.split(",");
		Set<DayOfWeek> result = EnumSet.noneOf(DayOfWeek.class);
		for (String part : parts) {
			String trimmed = part.trim();
			if (!trimmed.isEmpty()) {
				result.add(DayOfWeek.valueOf(trimmed));
			}
		}
		return result;
	}

	private LocalDate nextRepeatDay(LocalDate baseDate, Set<DayOfWeek> repeatDays) {
		for (int i = 1; i <= MAX_LOOKAHEAD_DAYS; i++) {
			LocalDate candidate = baseDate.plusDays(i);
			if (repeatDays.contains(candidate.getDayOfWeek())) {
				return candidate;
			}
		}
		return null;
	}

	private LocalDateTime moveToNextRepeatDay(LocalDate baseDate, Set<DayOfWeek> repeatDays, LocalTime activeStart) {
		LocalDate nextRepeat = nextRepeatDay(baseDate, repeatDays);
		if (nextRepeat == null) {
			return null;
		}
		return LocalDateTime.of(nextRepeat, activeStart);
	}

	private double toScore(LocalDateTime time) {
		return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}
}
