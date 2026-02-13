package com.raisedeveloper.server.domain.user.dto;

import java.time.LocalDateTime;

import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;

public record AlarmSettingsDndResponse(
	boolean dnd,
	LocalDateTime dndFinishedAt
) {
	public static AlarmSettingsDndResponse from(UserAlarmSettings settings) {
		return new AlarmSettingsDndResponse(settings.isDnd(), settings.getDndFinishedAt());
	}
}
