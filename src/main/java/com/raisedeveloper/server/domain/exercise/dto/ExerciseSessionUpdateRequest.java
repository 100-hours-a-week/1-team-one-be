package com.raisedeveloper.server.domain.exercise.dto;

import java.time.LocalDateTime;
import java.util.List;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ExerciseSessionUpdateRequest(
	@NotNull(message = EXERCISE_SESSION_START_AT_REQUIRED)
	LocalDateTime startAt,

	@NotNull(message = EXERCISE_SESSION_END_AT_REQUIRED)
	LocalDateTime endAt,

	@NotNull(message = EXERCISE_SESSION_RESULTS_REQUIRED)
	@Valid
	List<ExerciseResultRequest> exerciseResult
) {
}
