package com.raisedeveloper.server.domain.routine.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.routine.application.RoutineGenerationJobService;
import com.raisedeveloper.server.domain.routine.application.RoutineService;
import com.raisedeveloper.server.domain.routine.dto.AiRoutineCallbackRequest;
import com.raisedeveloper.server.domain.routine.dto.RoutinePlanResponse;
import com.raisedeveloper.server.global.response.ApiResponse;
import com.raisedeveloper.server.global.security.utils.AuthUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RoutineController {

	private final RoutineService routineService;
	private final RoutineGenerationJobService routineGenerationJobService;

	@GetMapping("/users/me/routines")
	public ApiResponse<RoutinePlanResponse> getMyRoutinePlan() {
		Long userId = AuthUtils.resolveUserIdFromContext();
		return ApiResponse.success("GET_ME_ROUTINES_SUCCESS", routineService.getMyRoutine(userId));
	}

	@PostMapping("routines/callback")
	public ApiResponse<Object> receiveCallback(
		@RequestBody AiRoutineCallbackRequest request
	) {
		routineGenerationJobService.handleCallback(request);
		return ApiResponse.success("AI_ROUTINE_CALLBACK_RECEIVED", java.util.Map.of());
	}
}
