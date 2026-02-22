package com.raisedeveloper.server.domain.exercise.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.Exercise;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseResult;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseListResponse;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseRepository;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseResultRepository;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.exercise.mapper.ExerciseMapper;
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
	private final ExerciseMapper exerciseMapper;

	@Transactional
	public ExerciseSession createSession(User user) {
		Routine activeRoutine = routineRepository.findLeastRecentlyUsedByUserId(user.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

		ExerciseSession session = new ExerciseSession(user, activeRoutine);
		ExerciseSession savedSession = exerciseSessionRepository.save(session);

		List<RoutineStep> routineSteps = routineStepRepository
			.findByRoutineIdWithExercise(activeRoutine.getId());

		List<ExerciseResult> results = routineSteps.stream()
			.map(step -> new ExerciseResult(savedSession, step))
			.toList();

		exerciseResultRepository.saveAll(results);
		activeRoutine.markUsed(LocalDateTime.now());

		return savedSession;
	}

	@Cacheable(cacheNames = "exerciseList")
	public ExerciseListResponse getAllExercises() {
		List<Exercise> exercises = exerciseRepository.findByIsDeprecatedFalse();
		return exerciseMapper.toExerciseListResponse(exercises);
	}
}
