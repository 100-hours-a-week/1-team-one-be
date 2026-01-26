package com.raisedeveloper.server.domain.exercise.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
	public void createSessionsForAlarms() {
		log.info("세션 생성 스케줄러 시작");
		long startTime = System.currentTimeMillis();

		LocalDateTime now = LocalDateTime.now();
		LocalTime currentTime = now.toLocalTime();
		DayOfWeek currentDay = now.getDayOfWeek();

		List<UserAlarmSettings> activeAlarmSettings = userAlarmSettingsRepository
			.findActiveAlarmSettings(currentTime, currentDay.name(), now);

		log.info("세션 생성 대상 알람 설정 조회 완료: {} 건", activeAlarmSettings.size());

		int createdCount = 0;

		for (UserAlarmSettings alarmSettings : activeAlarmSettings) {
			if (shouldCreateSession(alarmSettings, now, currentTime)) {
				User user = alarmSettings.getUser();
				try {
					ExerciseSession session = exerciseService.createSession(user);
					try {
						pushService.sendSessionNotification(user, session);
						createdCount++;
					} catch (Exception e) {
						log.error("푸시 알림 전송 실패 - userId: {}, sessionId: {}", user.getId(), session.getId(), e);
					}
				} catch (CustomException e) {
					if (e.getErrorCode() == ErrorCode.ROUTINE_NOT_FOUND) {
						log.warn("활성 루틴이 없어 세션 생성 건너뜀 - userId: {}", user.getId());
					}
				} catch (Exception e) {
					log.error("세션 생성 실패 - userId: {}", alarmSettings.getUser().getId(), e);
				}
			}
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		log.info("세션 생성 스케줄러 완료: 생성 건수={}, 소요 시간={}ms", createdCount, elapsedTime);
	}

	private boolean shouldCreateSession(
		UserAlarmSettings alarmSettings,
		LocalDateTime now,
		LocalTime currentTime
	) {
		if (isInFocusTime(currentTime, alarmSettings)) {
			log.debug("집중 시간으로 세션 생성 제외 - userId: {}", alarmSettings.getUser().getId());
			return false;
		}

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

		if (focusStart.isAfter(focusEnd)) {
			return currentTime.isAfter(focusStart) || currentTime.isBefore(focusEnd);
		}

		return !currentTime.isBefore(focusStart) && !currentTime.isAfter(focusEnd);
	}
}
