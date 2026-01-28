package com.raisedeveloper.server.domain.exercise.dto;

import com.raisedeveloper.server.domain.routine.domain.RoutineStep;

public record RoutineStepResponse(
	Long routineStepId,
	int stepOrder,
	Short targetReps,
	Short durationTime,
	int limitTime,
	ExerciseResponse exercise
) {
	public static RoutineStepResponse from(RoutineStep routineStep) {
		return new RoutineStepResponse(
			routineStep.getId(),
			routineStep.getStepOrder(),
			routineStep.getTargetReps(),
			routineStep.getDurationTime(),
			routineStep.getLimitTime(),
			ExerciseResponse.from(routineStep.getExercise())
		);
	}
}
