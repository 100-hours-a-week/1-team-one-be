package com.raisedeveloper.server.domain.routine.client.dto;

public record AiRoutineAsyncRequest(
	String taskId,
	Long userId,
	AiSurveyData surveyData
) {
}
