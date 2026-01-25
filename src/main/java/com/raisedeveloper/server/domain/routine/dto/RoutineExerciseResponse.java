package com.raisedeveloper.server.domain.routine.dto;

public record RoutineExerciseResponse(
	Long exerciseId,
	String name,
	String content,
	String reason
) {
}
