package com.raisedeveloper.server.domain.auth.api;

import static com.raisedeveloper.server.domain.common.ValidationConstants.*;
import static com.raisedeveloper.server.global.validation.RegexPatterns.*;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.auth.application.AuthService;
import com.raisedeveloper.server.domain.auth.dto.AuthLoginRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthLoginResponse;
import com.raisedeveloper.server.domain.auth.dto.AuthRefreshRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthRefreshResponse;
import com.raisedeveloper.server.domain.auth.dto.AuthSignUpRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthSignUpResponse;
import com.raisedeveloper.server.domain.auth.dto.AvailabilityResponse;
import com.raisedeveloper.server.global.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
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

	@GetMapping("/email-availability")
	public ApiResponse<AvailabilityResponse> checkEmailAvailability(
		@Email(message = EMAIL_FORMAT_INVALID)
		@NotBlank(message = EMAIL_REQUIRED)
		@RequestParam("email") String email
	) {
		return ApiResponse.success(
			"USER_EMAIL_AVAILABLE",
			new AvailabilityResponse(authService.isEmailAvailable(email))
		);
	}

	@GetMapping("/nickname-availability")
	public ApiResponse<AvailabilityResponse> checkNicknameAvailability(
		@NotBlank(message = NICKNAME_REQUIRED)
		@Size(max = 10, message = NICKNAME_LENGTH_INVALID)
		@Pattern(regexp = NICKNAME_REGEX, message = NICKNAME_FORMAT_INVALID)
		@RequestParam("nickname") String nickname
	) {
		return ApiResponse.success(
			"USER_NICKNAME_AVAILABLE",
			new AvailabilityResponse(authService.isNicknameAvailable(nickname))
		);
	}
}
