package com.raisedeveloper.server.domain.exercise.dto;

import java.time.LocalDateTime;

public record ExerciseSessionReportCreateResponse(
	Long sessionId,
	Long sessionReportId,
	Long routineId,
	LocalDateTime startAt,
	LocalDateTime endAt,
	Boolean isRoutineCompleted
) {
}
