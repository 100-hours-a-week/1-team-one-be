package com.raisedeveloper.server.domain.survey.dto;

public record SurveyOptionResponse(
	Long optionId,
	short sortOrder,
	String content
) {
}
