package com.raisedeveloper.server.domain.exercise.scheduler;

import java.time.LocalDateTime;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.exercise.application.AlarmScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmScheduleBootstrap implements ApplicationRunner {

	private final AlarmScheduleService alarmScheduleService;

	@Override
	public void run(ApplicationArguments args) {
		log.info("Alarm schedule bootstrap start");
		alarmScheduleService.refreshAll(LocalDateTime.now());
		log.info("Alarm schedule bootstrap complete");
	}
}
