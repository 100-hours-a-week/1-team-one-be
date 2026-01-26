package com.raisedeveloper.server.domain.routine.dto;

import java.util.List;

import com.raisedeveloper.server.domain.common.enums.RoutineStatus;

public record RoutinePlanResponse(
	RoutineStatus status,
	Long routineId,
	List<RoutineExerciseResponse> exercises
) {
}
