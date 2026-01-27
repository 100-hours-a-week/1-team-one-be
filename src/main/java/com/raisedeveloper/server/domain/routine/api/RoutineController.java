package com.raisedeveloper.server.domain.routine.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.routine.application.RoutineService;
import com.raisedeveloper.server.domain.routine.dto.RoutinePlanResponse;
import com.raisedeveloper.server.global.response.ApiResponse;
import com.raisedeveloper.server.global.security.utils.AuthUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users/me/routines")
@RequiredArgsConstructor
public class RoutineController {

	private final RoutineService routineService;

	@GetMapping
	public ApiResponse<RoutinePlanResponse> getMyRoutinePlan() {
		Long userId = AuthUtils.resolveUserIdFromContext();
		return ApiResponse.success("GET_ME_ROUTINES_SUCCESS", routineService.getMyRoutine(userId));
	}
}
