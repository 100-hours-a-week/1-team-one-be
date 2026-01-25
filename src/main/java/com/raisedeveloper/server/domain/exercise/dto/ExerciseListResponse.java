package com.raisedeveloper.server.domain.exercise.dto;

import java.util.List;

public record ExerciseListResponse(
	List<ExerciseResponse> exercises
) {
	public static ExerciseListResponse from(List<ExerciseResponse> exercises) {
		return new ExerciseListResponse(exercises);
	}
}
