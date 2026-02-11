package com.raisedeveloper.server.domain.user.dto;

import static com.raisedeveloper.server.global.validation.RegexPatterns.*;
import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AlarmSettingsRequest(
	@NotNull(message = USER_ALARM_INTERVAL_REQUIRED)
	@Min(value = 10, message = USER_ALARM_INTERVAL_MIN)
	@Max(value = 600, message = USER_ALARM_INTERVAL_MAX)
	short interval,

	@NotNull(message = USER_ALARM_ACTIVE_START_REQUIRED)
	LocalTime activeStartAt,

	@NotNull(message = USER_ALARM_ACTIVE_END_REQUIRED)
	LocalTime activeEndAt,

	LocalTime focusStartAt,

	LocalTime focusEndAt,

	@NotEmpty(message = USER_ALARM_REPEAT_DAYS_REQUIRED)
	List<@Pattern(regexp = REPEAT_DAY_REGEX, message = USER_ALARM_REPEAT_DAY_INVALID) String> repeatDays
) {
}
