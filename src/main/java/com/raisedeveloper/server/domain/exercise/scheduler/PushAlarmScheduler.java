package com.raisedeveloper.server.domain.exercise.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.application.ExerciseService;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.push.application.PushService;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;
import com.raisedeveloper.server.domain.user.infra.UserAlarmSettingsRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushAlarmScheduler {

	private final UserAlarmSettingsRepository userAlarmSettingsRepository;
	private final ExerciseSessionRepository exerciseSessionRepository;
	private final PushService pushService;
	private final ExerciseService exerciseService;

	@Scheduled(cron = "0 */1 * * * *")
	@Transactional
	public void processAlarms() {
		log.info("푸시 알람 스케줄러 시작");
		long startTime = System.currentTimeMillis();

		LocalDateTime now = LocalDateTime.now();
		LocalTime currentTime = now.toLocalTime();
		DayOfWeek currentDay = now.getDayOfWeek();

		List<UserAlarmSettings> activeAlarmSettings = userAlarmSettingsRepository
			.findActiveAlarmSettings(currentTime, currentDay.name());

		log.info("active 시간/요일 대상 알람 설정 조회 완료: {} 건", activeAlarmSettings.size());

		List<UserAlarmSettings> eligibleAlarmSettings = filterEligibleAlarmSettings(
			activeAlarmSettings, now, currentTime
		);

		log.info("알람 전송 대상 알람 설정 필터 완료: {} 건", eligibleAlarmSettings.size());

		List<ExerciseSession> createdSessions = createSessions(eligibleAlarmSettings);
		log.info("세션 생성 완료: {} 건", createdSessions.size());

		sendSessionAlarms(createdSessions);

		long elapsedTime = System.currentTimeMillis() - startTime;
		log.info("푸시 알람 스케줄러 완료: 세션 생성 건수={}, 소요 시간={}ms",
			createdSessions.size(), elapsedTime);
	}

	private List<UserAlarmSettings> filterEligibleAlarmSettings(
		List<UserAlarmSettings> activeAlarmSettings,
		LocalDateTime now,
		LocalTime currentTime
	) {
		return activeAlarmSettings.stream()
			.filter(alarmSettings -> {
				if (isInFocusTime(currentTime, alarmSettings)) {
					log.debug("집중 시간으로 알람 대상 제외 - userId: {}", alarmSettings.getUser().getId());
					return false;
				}
				return true;
			})
			.filter(alarmSettings -> {
				if (isInDnd(now, alarmSettings)) {
					log.debug("DND 활성화로 알람 대상 제외 - userId: {}", alarmSettings.getUser().getId());
					return false;
				}
				return true;
			})
			.filter(alarmSettings -> isIntervalElapsed(alarmSettings, now))
			.toList();
	}

	private boolean isIntervalElapsed(
		UserAlarmSettings alarmSettings,
		LocalDateTime now
	) {
		return exerciseSessionRepository.findLatestByUserId(alarmSettings.getUser().getId())
			.map(lastSession -> {
				long minutesSinceLastSession = ChronoUnit.MINUTES.between(
					lastSession.getCreatedAt(),
					now
				);

				if (minutesSinceLastSession < alarmSettings.getAlarmInterval()) {
					log.debug("알람 간격 미도달로 세션 생성 제외 - userId: {}, minutesSinceLastSession: {}, interval: {}",
						alarmSettings.getUser().getId(), minutesSinceLastSession, alarmSettings.getAlarmInterval());
					return false;
				}
				return true;
			})
			.orElse(true);
	}

	private boolean isInFocusTime(LocalTime currentTime, UserAlarmSettings alarmSettings) {
		LocalTime focusStart = alarmSettings.getFocusStartAt();
		LocalTime focusEnd = alarmSettings.getFocusEndAt();

		if (focusStart == null || focusEnd == null) {
			return false;
		}

		if (focusStart.isAfter(focusEnd)) {
			return currentTime.isAfter(focusStart) || currentTime.isBefore(focusEnd);
		}

		return !currentTime.isBefore(focusStart) && !currentTime.isAfter(focusEnd);
	}

	private boolean isInDnd(LocalDateTime now, UserAlarmSettings alarmSettings) {
		if (!alarmSettings.isDnd()) {
			return false;
		}

		LocalDateTime dndFinishedAt = alarmSettings.getDndFinishedAt();
		if (dndFinishedAt == null) {
			return true;
		}

		return dndFinishedAt.isAfter(now);
	}

	private List<ExerciseSession> createSessions(List<UserAlarmSettings> alarmSettingsList) {
		return alarmSettingsList.stream()
			.map(UserAlarmSettings::getUser)
			.map(this::tryCreateSession)
			.flatMap(Optional::stream)
			.toList();
	}

	private void sendSessionAlarms(List<ExerciseSession> sessions) {
		sessions.forEach(session -> trySendSessionAlarm(session.getUser(), session));
	}

	private Optional<ExerciseSession> tryCreateSession(User user) {
		try {
			return Optional.of(exerciseService.createSession(user));
		} catch (CustomException e) {
			if (e.getErrorCode() == ErrorCode.ROUTINE_NOT_FOUND) {
				log.warn("활성 루틴이 없어 세션 생성 건너뜀 - userId: {}", user.getId());
				return Optional.empty();
			}

			log.error("세션 생성 실패(CustomException) - userId: {}, code: {}", user.getId(), e.getErrorCode(), e);
			return Optional.empty();
		} catch (Exception e) {
			log.error("세션 생성 실패 - userId: {}", user.getId(), e);
			return Optional.empty();
		}
	}

	private void trySendSessionAlarm(User user, ExerciseSession session) {
		try {
			pushService.sendSessionPush(user, session);
		} catch (Exception e) {
			log.error("푸시 알림 전송 실패 - userId: {}, sessionId: {}", user.getId(), session.getId(), e);
		}
	}

}
