package com.raisedeveloper.server.domain.exercise.dto;

import java.time.LocalDateTime;

public record ExerciseSessionValidResponse(
	Long sessionId,
	Long routineId,
	LocalDateTime createdAt
) {
}
