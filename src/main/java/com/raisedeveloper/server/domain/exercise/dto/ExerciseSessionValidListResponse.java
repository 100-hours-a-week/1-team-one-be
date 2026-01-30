package com.raisedeveloper.server.domain.exercise.dto;

import java.util.List;

public record ExerciseSessionValidListResponse(
	List<ExerciseSessionValidResponse> sessions
) {
}
