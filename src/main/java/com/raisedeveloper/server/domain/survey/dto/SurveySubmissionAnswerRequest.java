package com.raisedeveloper.server.domain.survey.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import jakarta.validation.constraints.NotNull;

public record SurveySubmissionAnswerRequest(
	@NotNull(message = SURVEY_QUESTION_ID_REQUIRED_MESSAGE)
	Long questionId,

	@NotNull(message = SURVEY_OPTION_ID_REQUIRED_MESSAGE)
	Long optionId
) {
}
