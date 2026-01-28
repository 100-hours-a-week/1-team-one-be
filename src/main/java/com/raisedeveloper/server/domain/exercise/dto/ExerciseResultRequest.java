package com.raisedeveloper.server.domain.exercise.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.raisedeveloper.server.domain.exercise.enums.ExerciseResultStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExerciseResultRequest(
	@NotNull
	Long routineStepId,

	@NotNull
	ExerciseResultStatus status,

	@NotNull
	JsonNode pose_record,

	@NotNull
	@Min(0)
	@Max(100)
	Integer accuracy,

	@NotNull
	LocalDateTime startAt,

	@NotNull
	LocalDateTime endAt
) {
}
