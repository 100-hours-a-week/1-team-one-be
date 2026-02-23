package com.raisedeveloper.server.domain.exercise.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ExerciseSessionResponse(
	Long routineId,
	int routineOrder,
	LocalDateTime createdAt,
	List<RoutineStepResponse> routineSteps
) {
}
