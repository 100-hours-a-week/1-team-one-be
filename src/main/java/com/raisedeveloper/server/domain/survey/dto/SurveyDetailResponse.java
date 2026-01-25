package com.raisedeveloper.server.domain.survey.dto;

import java.util.List;

public record SurveyDetailResponse(
	Long surveyId,
	List<SurveyQuestionResponse> questions
) {
}
