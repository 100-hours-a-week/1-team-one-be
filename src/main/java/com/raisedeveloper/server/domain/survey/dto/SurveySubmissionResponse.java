package com.raisedeveloper.server.domain.survey.dto;

import com.raisedeveloper.server.domain.common.enums.RoutineGenerationJobStatus;

public record SurveySubmissionResponse(
	Long submissionId,
	String jobId,
	RoutineGenerationJobStatus status
) {
}
