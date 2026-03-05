package com.raisedeveloper.server.domain.exercise.dto;

import java.time.LocalDateTime;

public record ExerciseSessionReportSummaryDto(
	Long sessionReportId,
	LocalDateTime createdAt
) {
}
