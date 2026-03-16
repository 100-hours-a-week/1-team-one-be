package com.raisedeveloper.server.domain.satisfaction.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import jakarta.validation.constraints.NotNull;

public record SatisfactionVoteRequest(
	@NotNull(message = EXERCISE_SESSION_SATISFACTION_ROUTINE_REQUIRED_MESSAGE)
	Long routineId,

	@NotNull(message = EXERCISE_SESSION_SATISFACTION_REQUIRED_MESSAGE)
	Boolean satisfied
) {
}
