package com.raisedeveloper.server.domain.userprofile.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import com.raisedeveloper.server.domain.userprofile.application.UserProfileBatchSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileBatchScheduler {

	private final UserProfileBatchSyncService userProfileBatchSyncService;

	@Scheduled(
		cron = "${ai.server.user-profile.cron:0 0 4 * * *}",
		scheduler = "defaultTaskScheduler"
	)
	@SchedulerLock(name = "UserProfileBatchScheduler.syncDailyUserProfiles", lockAtMostFor = "PT1H")
	public void syncDailyUserProfiles() {
		log.info("사용자 프로필 배치 동기화 시작");
		userProfileBatchSyncService.syncAllUserProfiles();
		log.info("사용자 프로필 배치 동기화 종료");
	}
}
