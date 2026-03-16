package com.raisedeveloper.server.domain.user.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import com.raisedeveloper.server.domain.user.application.LeaderboardSnapshotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderboardSnapshotScheduler {

	private final LeaderboardSnapshotService leaderboardSnapshotService;

	@Scheduled(
		cron = "${app.leaderboard.snapshot.cron:0 0 * * * *}",
		scheduler = "defaultTaskScheduler"
	)
	@SchedulerLock(name = "LeaderboardSnapshotScheduler.rebuildSnapshot", lockAtMostFor = "PT30M")
	public void rebuildSnapshot() {
		log.info("Leaderboard snapshot rebuild started");
		leaderboardSnapshotService.rebuildSnapshot();
		log.info("Leaderboard snapshot rebuild finished");
	}
}
