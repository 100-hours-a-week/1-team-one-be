package com.raisedeveloper.server.domain.exercise.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ExerciseSessionUpdateRequest(
	@NotNull
	LocalDateTime startAt,

	@NotNull
	LocalDateTime endAt,

	@NotNull
	@Valid
	List<ExerciseResultRequest> exerciseResult
) {
}
