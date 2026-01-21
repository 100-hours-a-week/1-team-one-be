package com.raisedeveloper.server.domain.auth.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.auth.application.AuthService;
import com.raisedeveloper.server.domain.auth.dto.AuthLoginRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthLoginResponse;
import com.raisedeveloper.server.domain.auth.dto.AuthRefreshRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthRefreshResponse;
import com.raisedeveloper.server.domain.auth.dto.AuthSignUpRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthSignUpResponse;
import com.raisedeveloper.server.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/sign-up")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<AuthSignUpResponse> signup(@Valid @RequestBody AuthSignUpRequest request) {
		return ApiResponse.success("AUTH_SIGNUP_SUCCESS", authService.signup(request));
	}

	@PostMapping("/login")
	public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
		return ApiResponse.success("AUTH_LOGIN_SUCCESS", authService.login(request));
	}

	@PostMapping("/refresh")
	public ApiResponse<AuthRefreshResponse> refresh(@Valid @RequestBody AuthRefreshRequest request) {
		return ApiResponse.success("AUTH_REFRESH_SUCCESS", authService.refresh(request));
	}
}
