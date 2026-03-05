package com.raisedeveloper.server.domain.exercise.dto;

import java.util.List;

public record ExerciseSessionReportListResponse(
	List<ExerciseSessionReportSummaryDto> reports
) {
}
