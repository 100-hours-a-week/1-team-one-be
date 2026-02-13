package com.raisedeveloper.server.domain.user.dto;

import java.util.Arrays;
import java.util.List;

import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;

public record UserMeAlarmSettingsResponse(
	AlarmSettingsResponse alarmSettings
) {
	public static UserMeAlarmSettingsResponse from(UserAlarmSettings settings) {
		List<String> repeatDays = settings.getRepeatDays() == null || settings.getRepeatDays().isBlank()
			? List.of()
			: Arrays.asList(settings.getRepeatDays().split(","));

		return new UserMeAlarmSettingsResponse(
			new AlarmSettingsResponse(
				settings.getAlarmInterval(),
				settings.getActiveStartAt(),
				settings.getActiveEndAt(),
				settings.getFocusStartAt(),
				settings.getFocusEndAt(),
				repeatDays
			));
	}
}
