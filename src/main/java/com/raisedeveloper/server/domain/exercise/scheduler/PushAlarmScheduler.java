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
import com.raisedeveloper.server.domain.exercise.application.AlarmSessionDispatchService;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
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
	private static final int MAX_DRAIN_PER_RUN = 20_000;
	private static final long MAX_RUN_MILLIS = 50_000L;

	private final UserAlarmSettingsRepository userAlarmSettingsRepository;
	private final AlarmSessionDispatchService alarmSessionDispatchService;
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

		LocalDateTime dueCutoff = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
		int loopCount = 0;
		int fetchedDueTotal = 0;
		int processedSettingsTotal = 0;
		int createdSessionsTotal = 0;

		while (true) {
			long elapsed = System.currentTimeMillis() - startTime;
			if (elapsed >= MAX_RUN_MILLIS) {
				log.warn("드레인 루프 중단(실행 시간 제한 초과) - elapsedMs: {}, maxMs: {}, loops: {}",
					elapsed, MAX_RUN_MILLIS, loopCount);
				break;
			}
			if (fetchedDueTotal >= MAX_DRAIN_PER_RUN) {
				log.warn("드레인 루프 중단(최대 처리 건수 도달) - fetchedDueTotal: {}, maxPerRun: {}, loops: {}",
					fetchedDueTotal, MAX_DRAIN_PER_RUN, loopCount);
				break;
			}

			int remaining = MAX_DRAIN_PER_RUN - fetchedDueTotal;
			int currentBatchSize = Math.min(DUE_BATCH_SIZE, remaining);
			if (currentBatchSize <= 0) {
				break;
			}

			long batchStart = System.currentTimeMillis();
			List<Long> dueUserIds = alarmScheduleService.fetchDueUserIds(dueCutoff, currentBatchSize);
			if (dueUserIds.isEmpty()) {
				if (loopCount == 0) {
					log.info("[Drain] 처리 대상 없음");
				}
				break;
			}

			List<UserAlarmSettings> settings = userAlarmSettingsRepository.findByUserIdInWithUser(dueUserIds);
			List<ExerciseSession> createdSessions = createSessions(settings);
			advanceAfterDue(settings);

			loopCount++;
			fetchedDueTotal += dueUserIds.size();
			processedSettingsTotal += settings.size();
			createdSessionsTotal += createdSessions.size();

			log.info("[Drain-{}] due 조회: {}건, 설정 처리: {}건, 세션 생성: {}건, 소요시간: {}ms",
				loopCount,
				dueUserIds.size(),
				settings.size(),
				createdSessions.size(),
				System.currentTimeMillis() - batchStart);
		}

		long totalTime = System.currentTimeMillis() - startTime;

		log.info("========================================");
		log.info("[총합] 전체 소요시간: {}ms", totalTime);
		log.info("  - 드레인 루프 횟수: {}", loopCount);
		log.info("  - due 조회 누계: {}", fetchedDueTotal);
		log.info("  - 설정 처리 누계: {}", processedSettingsTotal);
		log.info("  - 세션 생성 누계: {}", createdSessionsTotal);
		log.info("  - 배치 크기/상한: {}/{}", DUE_BATCH_SIZE, MAX_DRAIN_PER_RUN);
		log.info("  - 실행 시간 상한(ms): {}", MAX_RUN_MILLIS);
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

	private Optional<ExerciseSession> tryCreateSession(UserAlarmSettings settings) {
		User user = settings.getUser();
		LocalDateTime scheduledAt = settings.getNextFireAt();
		try {
			return Optional.of(alarmSessionDispatchService.createSessionAndPublish(user, scheduledAt));
		} catch (CustomException e) {
			log.error("세션 생성/이벤트 저장 실패(CustomException) - userId: {}, code: {}",
				user.getId(), e.getErrorCode(), e);
			return Optional.empty();
		} catch (Exception e) {
			log.error("세션 생성/이벤트 저장 실패 - userId: {}", user.getId(), e);
			return Optional.empty();
		}
	}
}
