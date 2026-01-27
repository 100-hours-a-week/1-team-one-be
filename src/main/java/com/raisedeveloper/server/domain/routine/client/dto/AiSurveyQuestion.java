package com.raisedeveloper.server.domain.routine.client.dto;

public record AiSurveyQuestion(
	String questionContent,
	byte selectedOptionSortOrder
) {
}
