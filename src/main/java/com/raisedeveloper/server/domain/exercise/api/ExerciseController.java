package com.raisedeveloper.server.domain.exercise.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.exercise.application.ExerciseService;
import com.raisedeveloper.server.domain.exercise.application.ExerciseSessionService;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseListResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionCompleteResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionUpdateRequest;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionValidListResponse;
import com.raisedeveloper.server.global.response.ApiResponse;
import com.raisedeveloper.server.global.security.utils.AuthUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ExerciseController {

	private final ExerciseService exerciseService;
	private final ExerciseSessionService exerciseSessionService;

	@GetMapping("/exercises")
	public ApiResponse<ExerciseListResponse> getAllExercises() {
		return ApiResponse.of("GET_EXERCISES_SUCCESS", exerciseService.getAllExercises());
	}

	@GetMapping("/me/exercise-sessions/valid")
	public ApiResponse<ExerciseSessionValidListResponse> getValidExerciseSessions() {
		Long userId = AuthUtils.resolveUserIdFromContext();
		ExerciseSessionValidListResponse response = exerciseSessionService
			.getValidExerciseSessions(userId);
		return ApiResponse.of("GET_VALID_EXERCISE_SESSION_SUCCESS", response);
	}

	@GetMapping("/me/exercise-sessions/{sessionId}")
	public ApiResponse<ExerciseSessionResponse> getExerciseSession(@PathVariable Long sessionId) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		ExerciseSessionResponse response = exerciseSessionService.getExerciseSession(userId, sessionId);
		return ApiResponse.of("_GET_SESSION_SUCCESS", response);
	}

	@PatchMapping("/me/exercise-sessions/{sessionId}")
	public ApiResponse<ExerciseSessionCompleteResponse> updateExerciseSession(
		@PathVariable Long sessionId,
		@Valid @RequestBody ExerciseSessionUpdateRequest request
	) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		ExerciseSessionCompleteResponse response = exerciseSessionService.completeExerciseSession(userId, sessionId,
			request);
		return ApiResponse.of("COMPLETE_EXERCISE_SESSION_SUCCESS", response);
	}
}
