package com.raisedeveloper.server.domain.exercise.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.routine.domain.RoutineStep;

public record ExerciseSessionResponse(
	Long routineId,
	int routineOrder,
	LocalDateTime createdAt,
	List<RoutineStepResponse> routineSteps
) {
	public static ExerciseSessionResponse of(ExerciseSession session, List<RoutineStep> routineSteps) {
		List<RoutineStepResponse> stepDtos = routineSteps.stream()
			.map(RoutineStepResponse::from)
			.toList();

		return new ExerciseSessionResponse(
			session.getRoutine().getId(),
			session.getRoutine().getRoutineOrder(),
			session.getCreatedAt(),
			stepDtos
		);
	}
}
