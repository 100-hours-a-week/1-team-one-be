package com.raisedeveloper.server.domain.exercise.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseListResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseResponse;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseRepository;
import com.raisedeveloper.server.domain.routine.domain.Routine;
import com.raisedeveloper.server.domain.routine.infra.RoutineRepository;
import com.raisedeveloper.server.domain.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {

	private final RoutineRepository routineRepository;
	private final ExerciseRepository exerciseRepository;

	@Transactional
	public ExerciseSession createSession(User user) {
		Routine activeRoutine = routineRepository.findActiveRoutineByUserId(user.getId())
			.orElseThrow(() -> new IllegalStateException("활성 루틴 없음"));

		return new ExerciseSession(user, activeRoutine);
	}

	public ExerciseListResponse getAllExercises() {
		List<ExerciseResponse> exercises = exerciseRepository.findAll()
			.stream()
			.map(ExerciseResponse::from)
			.toList();

		return ExerciseListResponse.from(exercises);
	}
}
