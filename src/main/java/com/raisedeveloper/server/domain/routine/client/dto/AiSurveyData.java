package com.raisedeveloper.server.domain.routine.client.dto;

import java.util.List;

public record AiSurveyData(
	Integer routineCount,
	List<AiSurveyQuestion> survey
) {
}
