package com.raisedeveloper.server.domain.routine.client.dto;

import java.util.List;

public record AiRoutineResponse(
	String taskId,
	String status,
	Integer progress,
	String currentStep,
	AiSummary summary,
	String errorMessage,
	String completedAt,
	List<AiRoutineDto> routines
) {
	public boolean isCompleted() {
		return "COMPLETED".equals(status);
	}

	public boolean hasRoutines() {
		return routines != null && !routines.isEmpty();
	}
}
