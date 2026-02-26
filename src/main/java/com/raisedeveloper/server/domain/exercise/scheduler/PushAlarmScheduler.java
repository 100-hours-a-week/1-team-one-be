package com.raisedeveloper.server.domain.exercise.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import com.raisedeveloper.server.domain.exercise.application.AlarmScheduleService;
import com.raisedeveloper.server.domain.exercise.application.ExerciseService;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.push.application.PushService;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;
import com.raisedeveloper.server.domain.user.infra.UserAlarmSettingsRepository;
import com.raisedeveloper.server.global.exception.CustomException;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushAlarmScheduler {

	private static final int DUE_BATCH_SIZE = 500;

	private final UserAlarmSettingsRepository userAlarmSettingsRepository;
	private final PushService pushService;
	private final ExerciseService exerciseService;
	private final AlarmScheduleService alarmScheduleService;
	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Scheduled(cron = "0 */1 * * * *")
	@SchedulerLock(name = "PushAlarmScheduler.processAlarms", lockAtMostFor = "PT5M")
	public void processAlarms() {
		SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
		Statistics statistics = sessionFactory.getStatistics();
		statistics.clear();
		statistics.setStatisticsEnabled(true);
		log.info("========================================");
		log.info("푸시 알람 스케줄러 시작 - {}", LocalDateTime.now());
		long startTime = System.currentTimeMillis();

		LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

		long step1Start = System.currentTimeMillis();
		long queriesBeforeStep1 = statistics.getQueryExecutionCount();
		long entitiesBeforeStep1 = statistics.getEntityLoadCount();

		List<Long> dueUserIds = alarmScheduleService.fetchDueUserIds(now, DUE_BATCH_SIZE);
		log.info("1단계 대상 사용자 수 : {}", dueUserIds.size());

		long step1Time = System.currentTimeMillis() - step1Start;
		long queriesInStep1 = statistics.getQueryExecutionCount() - queriesBeforeStep1;
		long entitiesInStep1 = statistics.getEntityLoadCount() - entitiesBeforeStep1;

		log.info("[Step 1] Redis 조회 완료: {}건, 소요시간: {}ms", dueUserIds.size(), step1Time);
		log.info("[Step 1] 쿼리 수: {}, 엔티티 로드 수: {}", queriesInStep1, entitiesInStep1);

		if (dueUserIds.isEmpty()) {
			log.info("[Step 1] 처리 대상 없음");
			return;
		}

		long step2Start = System.currentTimeMillis();
		long queriesBeforeStep2 = statistics.getQueryExecutionCount();
		long entitiesBeforeStep2 = statistics.getEntityLoadCount();

		List<UserAlarmSettings> settings = userAlarmSettingsRepository.findByUserIdInWithUser(dueUserIds);

		long step2Time = System.currentTimeMillis() - step2Start;
		long queriesInStep2 = statistics.getQueryExecutionCount() - queriesBeforeStep2;
		long entitiesInStep2 = statistics.getEntityLoadCount() - entitiesBeforeStep2;

		log.info("[Step 2] 설정 로드 완료: {}건 → {}건, 소요시간: {}ms",
			dueUserIds.size(), settings.size(), step2Time);
		log.info("[Step 2] 쿼리 수: {}, 엔티티 로드 수: {}",
			queriesInStep2, entitiesInStep2);

		long step3Start = System.currentTimeMillis();
		long queriesBeforeStep3 = statistics.getQueryExecutionCount();
		long entitiesBeforeStep3 = statistics.getEntityLoadCount();

		List<ExerciseSession> createdSessions = createSessions(settings);

		long step3Time = System.currentTimeMillis() - step3Start;
		long queriesInStep3 = statistics.getQueryExecutionCount() - queriesBeforeStep3;
		long entitiesInStep3 = statistics.getEntityLoadCount() - entitiesBeforeStep3;

		log.info("[Step 3] 세션 생성: {}건, 소요시간: {}ms",
			createdSessions.size(), step3Time);
		log.info("[Step 3] 쿼리 수: {}, 엔티티 로드 수: {}",
			queriesInStep3, entitiesInStep3);

		long step4Start = System.currentTimeMillis();
		long queriesBeforeStep4 = statistics.getQueryExecutionCount();
		long entitiesBeforeStep4 = statistics.getEntityLoadCount();

		sendSessionAlarms(createdSessions);
		advanceAfterDue(settings);

		long step4Time = System.currentTimeMillis() - step4Start;
		long queriesInStep4 = statistics.getQueryExecutionCount() - queriesBeforeStep4;
		long entitiesInStep4 = statistics.getEntityLoadCount() - entitiesBeforeStep4;

		log.info("[Step 4] 푸시 전송: {}건, 소요시간: {}ms",
			createdSessions.size(), step4Time);
		log.info("[Step 4] 쿼리 수: {}, 엔티티 로드 수: {}",
			queriesInStep4, entitiesInStep4);

		long totalTime = System.currentTimeMillis() - startTime;

		log.info("========================================");
		log.info("[총합] 전체 소요시간: {}ms", totalTime);
		log.info("  - Step 1 (조회): {}ms ({}%)",
			step1Time, String.format("%.1f", step1Time * 100.0 / totalTime));
		log.info("  - Step 2 (필터): {}ms ({}%)",
			step2Time, String.format("%.1f", step2Time * 100.0 / totalTime));
		log.info("  - Step 3 (세션): {}ms ({}%)",
			step3Time, String.format("%.1f", step3Time * 100.0 / totalTime));
		log.info("  - Step 4 (푸시): {}ms ({}%)",
			step4Time, String.format("%.1f", step4Time * 100.0 / totalTime));
		log.info("========================================");

		log.info("========================================");
		log.info("[전체 쿼리 통계]");
		log.info("  - 총 쿼리 수: {}", statistics.getQueryExecutionCount());
		log.info("  - PreparedStatement 수: {}", statistics.getPrepareStatementCount());
		log.info("  - 엔티티 로드 수: {}", statistics.getEntityLoadCount());
		log.info("  - 컬렉션 로드 수: {}", statistics.getCollectionLoadCount());
		log.info("========================================");
	}

	private List<ExerciseSession> createSessions(List<UserAlarmSettings> alarmSettingsList) {
		return alarmSettingsList.stream()
			.map(UserAlarmSettings::getUser)
			.map(this::tryCreateSession)
			.flatMap(Optional::stream)
			.toList();
	}

	private void advanceAfterDue(List<UserAlarmSettings> dueSettings) {
		for (UserAlarmSettings settings : dueSettings) {
			LocalDateTime scheduledAt = settings.getNextFireAt();
			if (scheduledAt == null) {
				continue;
			}
			alarmScheduleService.advanceAfterDue(settings, scheduledAt);
		}
	}

	private void sendSessionAlarms(List<ExerciseSession> sessions) {
		sessions.forEach(session -> trySendSessionAlarm(session.getUser(), session));
	}

	private Optional<ExerciseSession> tryCreateSession(User user) {
		try {
			return Optional.of(exerciseService.createSession(user));
		} catch (CustomException e) {
			log.error("세션 생성 실패(CustomException) - userId: {}, code: {}", user.getId(), e.getErrorCode(), e);
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
