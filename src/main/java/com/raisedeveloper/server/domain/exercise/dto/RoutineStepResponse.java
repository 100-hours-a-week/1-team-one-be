package com.raisedeveloper.server.domain.exercise.dto;

public record RoutineStepResponse(
	Long routineStepId,
	int stepOrder,
	Short targetReps,
	Short durationTime,
	int limitTime,
	ExerciseResponse exercise
) {
}
