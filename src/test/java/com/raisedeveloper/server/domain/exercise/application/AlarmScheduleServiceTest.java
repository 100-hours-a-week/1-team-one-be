package com.raisedeveloper.server.domain.exercise.application;

import static org.assertj.core.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;

class AlarmScheduleServiceTest {

	private final AlarmScheduleService alarmScheduleService = new AlarmScheduleService(null, null);

	@Test
	void calculateNextFireAtReturnsNullWhenFocusCoversEntireActiveWindow() {
		UserAlarmSettings settings = new UserAlarmSettings(
			new User("focus-wrap@test.com", "pw"),
			(short)10,
			LocalTime.of(0, 0),
			LocalTime.of(23, 50),
			LocalTime.of(0, 0),
			LocalTime.of(23, 50),
			allDays()
		);
		LocalDateTime baseTime = LocalDateTime.of(2026, 3, 18, 11, 0);

		LocalDateTime nextFireAt = alarmScheduleService.calculateNextFireAt(settings, baseTime);

		assertThat(nextFireAt).isNull();
	}

	@Test
	void calculateNextFireAtMovesToNextDayForFocusEndingAtActiveEnd() {
		UserAlarmSettings settings = new UserAlarmSettings(
			new User("focus-end@test.com", "pw"),
			(short)10,
			LocalTime.of(10, 0),
			LocalTime.of(16, 0),
			LocalTime.of(12, 0),
			LocalTime.of(16, 0),
			allDays()
		);
		LocalDateTime baseTime = LocalDateTime.of(2026, 3, 18, 12, 0);

		LocalDateTime nextFireAt = alarmScheduleService.calculateNextFireAt(settings, baseTime);

		assertThat(nextFireAt).isEqualTo(LocalDateTime.of(2026, 3, 19, 10, 10));
		assertThat(nextFireAt).isAfter(baseTime);
	}

	private String allDays() {
		return String.join(",", Arrays.stream(DayOfWeek.values())
			.map(Enum::name)
			.toList());
	}
}
