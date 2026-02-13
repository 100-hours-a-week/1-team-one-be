package com.raisedeveloper.server.domain.user.dto;

import java.time.LocalTime;
import java.util.List;

public record AlarmSettingsResponse(
	int interval,
	LocalTime activeStartAt,
	LocalTime activeEndAt,
	LocalTime focusStartAt,
	LocalTime focusEndAt,
	List<String> repeatDays
) {
}
