package com.raisedeveloper.server.domain.routine.dto;

import java.util.List;

import com.raisedeveloper.server.domain.common.enums.RoutineGenerationJobStatus;

public record RoutinePlanResponse(
	RoutineGenerationJobStatus status,
	Long activeSubmissionId,
	Long generatingSubmissionId,
	List<RoutineExerciseResponse> exercises
) {
}
