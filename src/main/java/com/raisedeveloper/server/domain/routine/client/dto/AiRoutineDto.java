package com.raisedeveloper.server.domain.routine.client.dto;

import java.util.List;

public record AiRoutineDto(
	Integer routineOrder,
	String reason,
	List<AiRoutineStepDto> steps
) {
}
