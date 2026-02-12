package com.raisedeveloper.server.domain.exercise.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import com.raisedeveloper.server.domain.exercise.enums.ExerciseResultStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExerciseResultRequest(
	@NotNull(message = EXERCISE_ROUTINE_STEP_ID_REQUIRED_MESSAGE)
	Long routineStepId,

	@NotNull(message = EXERCISE_RESULT_STATUS_REQUIRED_MESSAGE)
	ExerciseResultStatus status,

	@NotNull(message = EXERCISE_POSE_RECORD_REQUIRED_MESSAGE)
	JsonNode pose_record,

	@NotNull(message = EXERCISE_ACCURACY_REQUIRED_MESSAGE)
	@Min(value = 0, message = EXERCISE_ACCURACY_MIN_MESSAGE)
	@Max(value = 100, message = EXERCISE_ACCURACY_MAX_MESSAGE)
	Integer accuracy,

	@NotNull(message = EXERCISE_START_AT_REQUIRED_MESSAGE)
	LocalDateTime startAt,

	@NotNull(message = EXERCISE_END_AT_REQUIRED_MESSAGE)
	LocalDateTime endAt
) {
}
