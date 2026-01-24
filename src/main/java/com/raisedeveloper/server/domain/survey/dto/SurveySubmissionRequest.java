package com.raisedeveloper.server.domain.survey.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SurveySubmissionRequest(
	@NotNull
	Long surveyId,

	@NotEmpty
	List<@Valid SurveySubmissionAnswerRequest> responses
) {
}
