package com.raisedeveloper.server.domain.exercise.dto;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseType;
import com.raisedeveloper.server.domain.exercise.enums.ExerciseResultStatus;

public record SessionReportExerciseDto(
	Long exerciseId,
	String exerciseName,
	ExerciseType exerciseType,
	int stepOrder,
	ExerciseResultStatus status,
	Byte accuracy
) {
}
