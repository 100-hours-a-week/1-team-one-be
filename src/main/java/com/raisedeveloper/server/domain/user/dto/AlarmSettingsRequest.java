package com.raisedeveloper.server.domain.user.dto;

import static com.raisedeveloper.server.global.validation.RegexPatterns.*;

import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AlarmSettingsRequest(
	@NotNull
	@Min(10)
	@Max(600)
	short interval,

	@NotNull
	LocalTime activeStartAt,

	@NotNull
	LocalTime activeEndAt,

	LocalTime focusStartAt,

	LocalTime focusEndAt,

	@NotEmpty
	List<@Pattern(regexp = REPEAT_DAY_REGEX) String> repeatDays
) {
}
