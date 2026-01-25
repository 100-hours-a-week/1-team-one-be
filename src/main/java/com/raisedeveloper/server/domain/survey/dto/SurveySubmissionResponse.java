package com.raisedeveloper.server.domain.survey.dto;

public record SurveySubmissionResponse(
	Long submissionId
) {
	public static SurveySubmissionResponse from(Long submissionId) {
		return new SurveySubmissionResponse(submissionId);
	}
}
