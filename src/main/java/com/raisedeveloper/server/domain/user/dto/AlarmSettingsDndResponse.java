package com.raisedeveloper.server.domain.user.dto;

import java.time.LocalDateTime;

public record AlarmSettingsDndResponse(
	boolean dnd,
	LocalDateTime dndFinishedAt
) {
}
