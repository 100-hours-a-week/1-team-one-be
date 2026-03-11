package com.raisedeveloper.server.domain.satisfaction.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import com.raisedeveloper.server.domain.satisfaction.application.SatisfactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SatisfactionBatchScheduler {

	private final SatisfactionService satisfactionService;

	@Scheduled(
		cron = "${ai.server.satisfaction.cron:0 10 4 * * *}",
		scheduler = "defaultTaskScheduler"
	)
	@SchedulerLock(name = "SatisfactionBatchScheduler.syncDailyExerciseSatisfactions", lockAtMostFor = "PT1H")
	public void syncDailyExerciseSatisfactions() {
		log.info("운동 만족도 배치 동기화 시작");
		satisfactionService.syncAllExerciseSatisfactions();
		log.info("운동 만족도 배치 동기화 종료");
	}
}
