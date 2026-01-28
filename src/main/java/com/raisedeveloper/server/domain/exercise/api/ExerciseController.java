package com.raisedeveloper.server.domain.exercise.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.exercise.application.ExerciseService;
import com.raisedeveloper.server.domain.exercise.application.ExerciseSessionService;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseListResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionResponse;
import com.raisedeveloper.server.global.response.ApiResponse;
import com.raisedeveloper.server.global.security.utils.AuthUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ExerciseController {

	private final ExerciseService exerciseService;
	private final ExerciseSessionService exerciseSessionService;

	@GetMapping("/exercises")
	public ApiResponse<ExerciseListResponse> getAllExercises() {
		return ApiResponse.success("GET_EXERCISES_SUCCESS", exerciseService.getAllExercises());
	}

	@GetMapping("/me/exercise-sessions/{sessionId}")
	public ApiResponse<ExerciseSessionResponse> getExerciseSession(@PathVariable Long sessionId) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		ExerciseSessionResponse response = exerciseSessionService.getExerciseSession(userId, sessionId);
		return ApiResponse.success("_GET_SESSION_SUCCESS", response);
	}
}
