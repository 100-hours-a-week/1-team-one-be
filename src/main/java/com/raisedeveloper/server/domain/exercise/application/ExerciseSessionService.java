package com.raisedeveloper.server.domain.exercise.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionResponse;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.routine.domain.RoutineStep;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseSessionService {

	private final ExerciseSessionRepository exerciseSessionRepository;
	private final com.raisedeveloper.server.domain.routine.infra.RoutineStepRepository routineStepRepository;

	public ExerciseSessionResponse getExerciseSession(Long userId, Long sessionId) {
		ExerciseSession session = exerciseSessionRepository
			.findByIdAndUserIdWithRoutine(sessionId, userId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.EXERCISE_SESSION_NOT_FOUND)
			);

		List<RoutineStep> routineSteps = routineStepRepository
			.findByRoutineIdWithExercise(session.getRoutine().getId());

		log.info("Exercise session retrieved: sessionId={}, userId={}", sessionId, userId);

		return ExerciseSessionResponse.of(session, routineSteps);
	}
}
