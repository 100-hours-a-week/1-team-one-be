package com.raisedeveloper.server.domain.user.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record AlarmSettingsDndRequest(
	@NotNull
	LocalDateTime dndFinishedAt
) {
}
