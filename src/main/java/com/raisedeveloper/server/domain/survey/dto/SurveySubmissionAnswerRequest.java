package com.raisedeveloper.server.domain.survey.dto;

import jakarta.validation.constraints.NotNull;

public record SurveySubmissionAnswerRequest(
	@NotNull
	Long questionId,

	@NotNull
	Long optionId
) {
}
