package com.raisedeveloper.server.domain.exercise.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseResult;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseListResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseResponse;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseRepository;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseResultRepository;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.routine.domain.Routine;
import com.raisedeveloper.server.domain.routine.domain.RoutineStep;
import com.raisedeveloper.server.domain.routine.infra.RoutineRepository;
import com.raisedeveloper.server.domain.routine.infra.RoutineStepRepository;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {

	private final RoutineRepository routineRepository;
	private final ExerciseRepository exerciseRepository;
	private final ExerciseSessionRepository exerciseSessionRepository;
	private final ExerciseResultRepository exerciseResultRepository;
	private final RoutineStepRepository routineStepRepository;

	@Transactional
	public ExerciseSession createSession(User user) {
		Routine activeRoutine = routineRepository.findActiveRoutineByUserId(user.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

		ExerciseSession session = new ExerciseSession(user, activeRoutine);
		ExerciseSession savedSession = exerciseSessionRepository.saveAndFlush(session);

		List<RoutineStep> routineSteps = routineStepRepository
			.findByRoutineIdWithExercise(activeRoutine.getId());

		List<ExerciseResult> results = routineSteps.stream()
			.map(step -> new ExerciseResult(savedSession, step))
			.toList();

		exerciseResultRepository.saveAll(results);

		return savedSession;
	}

	public ExerciseListResponse getAllExercises() {
		List<ExerciseResponse> exercises = exerciseRepository.findAll()
			.stream()
			.map(ExerciseResponse::from)
			.toList();

		return ExerciseListResponse.from(exercises);
	}
}
