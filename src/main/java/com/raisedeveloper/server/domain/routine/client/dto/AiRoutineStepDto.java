package com.raisedeveloper.server.domain.routine.client.dto;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseType;

public record AiRoutineStepDto(
	String exerciseId,
	ExerciseType type,
	Integer stepOrder,
	Integer limitTime,
	Integer durationTime,
	Integer targetReps
) {
}
