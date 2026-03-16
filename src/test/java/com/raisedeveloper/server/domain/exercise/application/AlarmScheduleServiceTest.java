package com.raisedeveloper.server.domain.exercise.application;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;

class AlarmScheduleServiceTest {

	private final AlarmScheduleService alarmScheduleService = new AlarmScheduleService(null, null);

	@Test
	void calculateNextFireAt_movesToNextRepeatDay_whenFocusWindowConsumesRemainingActiveTime() {
		User user = new User("user46@example.com", "password");
		UserAlarmSettings settings = new UserAlarmSettings(
			user,
			(short)60,
			LocalTime.of(10, 0),
			LocalTime.of(16, 0),
			LocalTime.of(12, 0),
			LocalTime.of(16, 0),
			"MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY"
		);

		LocalDateTime nextFireAt = alarmScheduleService.calculateNextFireAt(
			settings,
			LocalDateTime.of(2026, 3, 16, 11, 0)
		);

		assertThat(nextFireAt).isEqualTo(LocalDateTime.of(2026, 3, 17, 10, 0));
	}
}
