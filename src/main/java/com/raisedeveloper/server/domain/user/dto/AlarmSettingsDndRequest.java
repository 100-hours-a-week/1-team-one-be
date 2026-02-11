package com.raisedeveloper.server.domain.user.dto;

import java.time.LocalDateTime;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import jakarta.validation.constraints.NotNull;

public record AlarmSettingsDndRequest(
	@NotNull(message = USER_DND_FINISHED_AT_REQUIRED)
	LocalDateTime dndFinishedAt
) {
}
