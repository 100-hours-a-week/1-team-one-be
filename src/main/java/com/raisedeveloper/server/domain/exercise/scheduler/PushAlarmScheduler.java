package com.raisedeveloper.server.domain.exercise.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import com.raisedeveloper.server.domain.exercise.application.AlarmScheduleService;
import com.raisedeveloper.server.domain.exercise.event.AlarmDueUserEvent;
import com.raisedeveloper.server.domain.exercise.event.AlarmEventPublisher;
import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;
import com.raisedeveloper.server.domain.user.infra.UserAlarmSettingsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushAlarmScheduler {

	private static final int DUE_BATCH_SIZE = 500;
	private static final Duration CLAIM_LEASE_DURATION = Duration.ofMinutes(5);
	private static final int MAX_DRAIN_PER_RUN = 20_000;
	private static final long MAX_RUN_MILLIS = 50_000L;

	private final UserAlarmSettingsRepository userAlarmSettingsRepository;
	private final AlarmScheduleService alarmScheduleService;
	private final AlarmEventPublisher alarmEventPublisher;

	@Scheduled(cron = "0 */1 * * * *", scheduler = "defaultTaskScheduler")
	@SchedulerLock(name = "PushAlarmScheduler.processAlarms", lockAtMostFor = "PT5M")
	public void processAlarms() {
		dispatchDueUsers();
	}

	private void dispatchDueUsers() {
		long startedAt = System.currentTimeMillis();
		LocalDateTime dueCutoff = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
		int totalClaimed = 0;
		int totalPublished = 0;
		int loopCount = 0;

		while (true) {
			long elapsed = System.currentTimeMillis() - startedAt;
			if (elapsed >= MAX_RUN_MILLIS) {
				log.warn("[Dispatcher] stop by runtime budget: elapsedMs={}, maxMs={}, loops={}",
					elapsed, MAX_RUN_MILLIS, loopCount);
				break;
			}
			if (totalClaimed >= MAX_DRAIN_PER_RUN) {
				log.warn("[Dispatcher] stop by drain cap: claimed={}, cap={}, loops={}",
					totalClaimed, MAX_DRAIN_PER_RUN, loopCount);
				break;
			}

			int remaining = MAX_DRAIN_PER_RUN - totalClaimed;
			int currentBatchSize = Math.min(DUE_BATCH_SIZE, remaining);
			if (currentBatchSize <= 0) {
				break;
			}

			List<Long> claimedUserIds = alarmScheduleService.claimDueUserIds(
				dueCutoff,
				currentBatchSize,
				CLAIM_LEASE_DURATION
			);
			if (claimedUserIds.isEmpty()) {
				if (loopCount == 0) {
					log.info("[Dispatcher] no due users");
				}
				break;
			}

			Map<Long, UserAlarmSettings> settingsByUserId = userAlarmSettingsRepository.findByUserIdInWithUser(claimedUserIds)
				.stream()
				.collect(Collectors.toMap(s -> s.getUser().getId(), Function.identity()));

			List<AlarmDueUserEvent> dueUserEvents = new ArrayList<>(claimedUserIds.size());
			for (Long userId : claimedUserIds) {
				UserAlarmSettings settings = settingsByUserId.get(userId);
				if (settings == null || settings.getNextFireAt() == null) {
					alarmScheduleService.removeFromSchedule(userId);
					continue;
				}

				dueUserEvents.add(new AlarmDueUserEvent(
					UUID.randomUUID().toString(),
					userId,
					settings.getNextFireAt(),
					LocalDateTime.now(),
					UUID.randomUUID().toString()
				));
			}
			alarmEventPublisher.publishDueUsers(dueUserEvents);
			int publishedInLoop = dueUserEvents.size();

			loopCount++;
			totalClaimed += claimedUserIds.size();
			totalPublished += publishedInLoop;
		}

		log.info("[Dispatcher] finished: claimed={}, published={}, loops={}, elapsedMs={}",
			totalClaimed, totalPublished, loopCount, System.currentTimeMillis() - startedAt);
	}
}
