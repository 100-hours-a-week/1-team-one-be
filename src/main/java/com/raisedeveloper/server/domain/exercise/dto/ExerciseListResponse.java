package com.raisedeveloper.server.domain.exercise.dto;

import java.util.List;

public record ExerciseListResponse(
	List<ExerciseResponse> exercises
) {
}
