package com.raisedeveloper.server.domain.survey.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SurveySubmissionRequest(
	@NotNull(message = SURVEY_ID_REQUIRED)
	Long surveyId,

	@NotEmpty(message = SURVEY_RESPONSES_REQUIRED)
	List<@Valid SurveySubmissionAnswerRequest> responses
) {
}
