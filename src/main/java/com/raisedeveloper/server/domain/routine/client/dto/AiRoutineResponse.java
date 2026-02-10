package com.raisedeveloper.server.domain.routine.client.dto;

import java.util.List;

import com.raisedeveloper.server.domain.common.enums.RoutineGenerationJobStatus;

public record AiRoutineResponse(
	RoutineGenerationJobStatus status,
	List<AiRoutineDto> routines
) {

	public boolean hasRoutines() {
		return routines != null && !routines.isEmpty();
	}
}
