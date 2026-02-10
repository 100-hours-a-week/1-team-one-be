package com.raisedeveloper.server.domain.exercise.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.exercise.application.ExerciseService;
import com.raisedeveloper.server.domain.exercise.application.AlarmScheduleService;
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
	public void processAlarms() {
		SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
		Statistics statistics = sessionFactory.getStatistics();
		statistics.clear();
		statistics.setStatisticsEnabled(true);
		log.info("========================================");
		log.info("푸시 알람 스케줄러 시작 - {}", LocalDateTime.now());
		long startTime = System.currentTimeMillis();
		logMemoryAndGc("start");

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

		List<DueAlarm> dueAlarms = resolveAndReschedule(settings, now);
		List<UserAlarmSettings> dueSettings = dueAlarms.stream()
			.map(DueAlarm::settings)
			.toList();
		List<ExerciseSession> createdSessions = createSessions(dueSettings);

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
		advanceAfterDue(dueAlarms, now);

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

		// 총 통계 출력
		log.info("========================================");
		log.info("[전체 쿼리 통계]");
		log.info("  - 총 쿼리 수: {}", statistics.getQueryExecutionCount());
		log.info("  - PreparedStatement 수: {}", statistics.getPrepareStatementCount());
		log.info("  - 엔티티 로드 수: {}", statistics.getEntityLoadCount());
		log.info("  - 컬렉션 로드 수: {}", statistics.getCollectionLoadCount());
		log.info("========================================");
		logMemoryAndGc("end");
	}

	private void logMemoryAndGc(String phase) {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
		MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();
		log.info("[{}] Heap used: {}MB, committed: {}MB, max: {}MB",
			phase,
			toMB(heap.getUsed()),
			toMB(heap.getCommitted()),
			toMB(heap.getMax()));
		log.info("[{}] Non-Heap used: {}MB, committed: {}MB, max: {}MB",
			phase,
			toMB(nonHeap.getUsed()),
			toMB(nonHeap.getCommitted()),
			toMB(nonHeap.getMax()));

		for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
			log.info("[{}] GC {}: count={}, timeMs={}",
				phase,
				gc.getName(),
				gc.getCollectionCount(),
				gc.getCollectionTime());
		}
	}

	private long toMB(long bytes) {
		if (bytes < 0) {
			return -1;
		}
		return bytes / (1024 * 1024);
	}

	private List<DueAlarm> resolveAndReschedule(
		List<UserAlarmSettings> alarmSettings,
		LocalDateTime now
	) {
		if (alarmSettings.isEmpty()) {
			return List.of();
		}

		List<DueAlarm> dueSettings = new ArrayList<>();
		for (UserAlarmSettings settings : alarmSettings) {
			LocalDateTime recalculated = alarmScheduleService.calculateNextFireAt(settings, now);
			if (recalculated == null) {
				alarmScheduleService.applyNextFireAt(settings, null);
				continue;
			}
			if (recalculated.isAfter(now)) {
				alarmScheduleService.applyNextFireAt(settings, recalculated);
				continue;
			}
			LocalDateTime scheduledAt = settings.getNextFireAt() != null ? settings.getNextFireAt() : recalculated;
			dueSettings.add(new DueAlarm(settings, scheduledAt));
		}
		return dueSettings;
	}

	private List<ExerciseSession> createSessions(List<UserAlarmSettings> alarmSettingsList) {
		return alarmSettingsList.stream()
			.map(UserAlarmSettings::getUser)
			.map(this::tryCreateSession)
			.flatMap(Optional::stream)
			.toList();
	}

	private void advanceAfterDue(List<DueAlarm> dueAlarms, LocalDateTime now) {
		for (DueAlarm due : dueAlarms) {
			alarmScheduleService.advanceAfterDue(due.settings(), due.scheduledAt(), now);
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

	private record DueAlarm(UserAlarmSettings settings, LocalDateTime scheduledAt) {
	}

}
