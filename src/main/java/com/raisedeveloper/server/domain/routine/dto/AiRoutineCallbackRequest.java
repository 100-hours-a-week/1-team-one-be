package com.raisedeveloper.server.domain.routine.dto;

import java.util.List;

import com.raisedeveloper.server.domain.common.enums.RoutineGenerationJobStatus;
import com.raisedeveloper.server.domain.routine.client.dto.AiRoutineDto;

public record AiRoutineCallbackRequest(
	String taskId,
	Long userId,
	RoutineGenerationJobStatus status,
	String errorMessage,
	List<AiRoutineDto> routines
) {
}
