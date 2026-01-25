package com.raisedeveloper.server.domain.exercise.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.dto.ExerciseListResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseResponse;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {

	private final ExerciseRepository exerciseRepository;

	public ExerciseListResponse getAllExercises() {
		List<ExerciseResponse> exercises = exerciseRepository.findAll()
			.stream()
			.map(ExerciseResponse::from)
			.toList();

		return ExerciseListResponse.from(exercises);
	}
}
