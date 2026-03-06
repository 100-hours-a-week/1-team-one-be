package com.raisedeveloper.server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

	@Value("${app.scheduler.default.pool-size:4}")
	private int defaultSchedulerPoolSize;

	@Value("${app.scheduler.outbox.pool-size:1}")
	private int outboxSchedulerPoolSize;

	@Bean
	public ThreadPoolTaskScheduler defaultTaskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(defaultSchedulerPoolSize);
		scheduler.setThreadNamePrefix("scheduled-default-");
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		scheduler.setAwaitTerminationSeconds(60);
		scheduler.setErrorHandler(
			throwable -> log.error("기본 스케줄러 실행 중 예외 발생", throwable)
		);
		return scheduler;
	}

	@Bean
	public ThreadPoolTaskScheduler outboxTaskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(outboxSchedulerPoolSize);
		scheduler.setThreadNamePrefix("scheduled-outbox-");
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		scheduler.setAwaitTerminationSeconds(60);
		scheduler.setErrorHandler(
			throwable -> log.error("Outbox 스케줄러 실행 중 예외 발생", throwable)
		);
		return scheduler;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setTaskScheduler(defaultTaskScheduler());
	}
}
