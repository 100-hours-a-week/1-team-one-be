package com.raisedeveloper.server.domain.user.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record AlarmSettingsDndRequest(
	@NotNull(message = USER_DND_FINISHED_AT_REQUIRED_MESSAGE)
	LocalDateTime dndFinishedAt
) {
}
