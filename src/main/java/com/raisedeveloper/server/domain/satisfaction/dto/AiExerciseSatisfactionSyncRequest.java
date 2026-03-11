package com.raisedeveloper.server.domain.satisfaction.dto;

import java.util.List;

public record AiExerciseSatisfactionSyncRequest(
	List<AiExerciseSatisfactionDto> records
) {
}
