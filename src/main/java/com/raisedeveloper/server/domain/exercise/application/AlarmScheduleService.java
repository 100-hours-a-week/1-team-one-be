package com.raisedeveloper.server.domain.exercise.application;

import java.time.DayOfWeek;
import java.time.Duration;
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
import org.springframework.data.redis.core.script.DefaultRedisScript;
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
	private static final String REDIS_PROCESSING_KEY = "alarm:processing_fire_at";
	private static final int MAX_LOOKAHEAD_DAYS = 7;
	private static final DefaultRedisScript<List> CLAIM_DUE_USERS_SCRIPT = createClaimScript();

	private final StringRedisTemplate redisTemplate;
	private final UserAlarmSettingsRepository userAlarmSettingsRepository;

	@Transactional
	public List<Long> claimDueUserIds(LocalDateTime now, int limit, Duration leaseDuration) {
		requeueExpiredProcessing(now);

		long nowScore = toEpochMilli(now);
		long leaseUntilScore = toEpochMilli(now.plus(leaseDuration));

		List<String> claimedUsers = redisTemplate.execute(
			CLAIM_DUE_USERS_SCRIPT,
			List.of(REDIS_KEY, REDIS_PROCESSING_KEY),
			String.valueOf(nowScore),
			String.valueOf(limit),
			String.valueOf(leaseUntilScore)
		);
		if (claimedUsers == null || claimedUsers.isEmpty()) {
			return List.of();
		}

		return claimedUsers.stream()
			.map(Long::valueOf)
			.toList();
	}

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
		redisTemplate.opsForZSet().remove(REDIS_PROCESSING_KEY, userId.toString());
	}

	public void markClaimCompleted(Long userId) {
		redisTemplate.opsForZSet().remove(REDIS_PROCESSING_KEY, userId.toString());
	}

	public void requeueExpiredProcessing(LocalDateTime now) {
		double nowScore = toScore(now);
		Set<String> expiredUsers = redisTemplate.opsForZSet()
			.rangeByScore(REDIS_PROCESSING_KEY, Double.NEGATIVE_INFINITY, nowScore);
		if (expiredUsers == null || expiredUsers.isEmpty()) {
			return;
		}
		for (String userId : expiredUsers) {
			redisTemplate.opsForZSet().remove(REDIS_PROCESSING_KEY, userId);
			redisTemplate.opsForZSet().add(REDIS_KEY, userId, nowScore);
		}
	}

	@Transactional
	public void advanceAfterDue(UserAlarmSettings settings, LocalDateTime scheduledAt) {
		LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
		LocalDateTime base = scheduledAt == null || scheduledAt.isBefore(now) ? now : scheduledAt;
		LocalDateTime nextFireAt = calculateNextFireAt(settings, base);
		applyNextFireAt(settings, nextFireAt);
	}

	public LocalDateTime calculateNextFireAt(
		UserAlarmSettings settings,
		LocalDateTime baseTime
	) {
		if (settings == null) {
			return null;
		}
		LocalTime activeStart = settings.getActiveStartAt();
		LocalTime activeEnd = settings.getActiveEndAt();
		if (activeStart == null || activeEnd == null) {
			return null;
		}

		// 1. 기준 일시 = 현재 + interval
		LocalDateTime threshold = baseTime.truncatedTo(ChronoUnit.MINUTES)
			.plusMinutes(settings.getAlarmInterval());

		// 2. DND 확인, 설정되어 있는 경우 다음 기준 일시 = dnd 종료 시각 + interval
		if (settings.isDnd()) {
			LocalDateTime dndFinishedAt = settings.getDndFinishedAt();
			if (dndFinishedAt != null && threshold.isBefore(dndFinishedAt)) {
				threshold = dndFinishedAt.plusMinutes(settings.getAlarmInterval());
			}
		}

		Set<DayOfWeek> repeatDays = parseRepeatDays(settings.getRepeatDays());
		if (repeatDays.isEmpty()) {
			return null;
		}

		LocalTime focusStart = settings.getFocusStartAt();
		LocalTime focusEnd = settings.getFocusEndAt();

		// 3. 반복 요일 내에서 후보 날짜 탐색
		// 해당 일자부터 시작하여 alarm_settings 설정 바탕으로 유효 슬롯 검색
		// 1주일 뒤로 밀리는 경우에는 스케줄링 하지 않고 null 반환
		for (int i = 0; i <= MAX_LOOKAHEAD_DAYS; i++) {
			LocalDate candidateDate = threshold.toLocalDate().plusDays(i);
			if (!repeatDays.contains(candidateDate.getDayOfWeek())) {
				continue;
			}

			LocalDateTime candidate = resolveCandidateForDate(
				candidateDate,
				threshold,
				activeStart,
				activeEnd,
				focusStart,
				focusEnd,
				settings.getAlarmInterval()
			);
			if (candidate != null) {
				return candidate;
			}
		}

		return null;
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

	private LocalDateTime resolveCandidateForDate(
		LocalDate candidateDate,
		LocalDateTime threshold,
		LocalTime activeStart,
		LocalTime activeEnd,
		LocalTime focusStart,
		LocalTime focusEnd,
		int intervalMinutes
	) {
		LocalDateTime windowStart = LocalDateTime.of(candidateDate, activeStart)
			.plusMinutes(intervalMinutes);
		LocalDateTime windowEnd = LocalDateTime.of(candidateDate, activeEnd);

		LocalDateTime candidate = candidateDate.equals(threshold.toLocalDate())
			? threshold
			: windowStart;
		if (candidate.isBefore(windowStart)) {
			candidate = windowStart;
		}
		if (candidate.isAfter(windowEnd)) {
			return null;
		}

		if (focusStart == null || focusEnd == null) {
			return candidate;
		}

		LocalDateTime focusStartAt = LocalDateTime.of(candidateDate, focusStart);
		LocalDateTime focusEndAt = LocalDateTime.of(candidateDate, focusEnd);
		boolean inFocus = !candidate.isBefore(focusStartAt) && !candidate.isAfter(focusEndAt);
		if (!inFocus) {
			return candidate;
		}

		LocalDateTime afterFocus = focusEndAt.plusMinutes(intervalMinutes);
		if (afterFocus.isBefore(windowStart)) {
			afterFocus = windowStart;
		}
		if (afterFocus.isAfter(windowEnd)) {
			return null;
		}

		return afterFocus;
	}

	private double toScore(LocalDateTime time) {
		return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	private long toEpochMilli(LocalDateTime time) {
		return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	private static DefaultRedisScript<List> createClaimScript() {
		DefaultRedisScript<List> script = new DefaultRedisScript<>();
		script.setResultType(List.class);
		script.setScriptText("""
			local dueKey = KEYS[1]
			local processingKey = KEYS[2]
			local nowScore = ARGV[1]
			local limit = tonumber(ARGV[2])
			local leaseUntil = ARGV[3]
			local candidates = redis.call('ZRANGEBYSCORE', dueKey, '-inf', nowScore, 'LIMIT', 0, limit)
			for i, member in ipairs(candidates) do
			  redis.call('ZREM', dueKey, member)
			  redis.call('ZADD', processingKey, leaseUntil, member)
			end
			return candidates
			""");
		return script;
	}
}
