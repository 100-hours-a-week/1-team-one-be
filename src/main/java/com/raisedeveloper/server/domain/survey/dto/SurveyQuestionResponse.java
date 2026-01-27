package com.raisedeveloper.server.domain.survey.dto;

import java.util.List;

public record SurveyQuestionResponse(
	Long questionId,
	short sortOrder,
	String content,
	List<SurveyOptionResponse> options
) {
}
