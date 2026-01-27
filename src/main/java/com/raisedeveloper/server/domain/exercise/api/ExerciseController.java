package com.raisedeveloper.server.domain.exercise.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.exercise.application.ExerciseService;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseListResponse;
import com.raisedeveloper.server.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ExerciseController {

	private final ExerciseService exerciseService;

	@GetMapping("/exercises")
	public ApiResponse<ExerciseListResponse> getAllExercises() {
		return ApiResponse.success("GET_EXERCISES_SUCCESS", exerciseService.getAllExercises());
	}
}
