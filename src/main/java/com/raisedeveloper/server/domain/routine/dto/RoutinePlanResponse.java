package com.raisedeveloper.server.domain.routine.dto;

import java.util.List;

public record RoutinePlanResponse(
	String status,
	Long routineId,
	List<RoutineExerciseResponse> exercises
) {
}
