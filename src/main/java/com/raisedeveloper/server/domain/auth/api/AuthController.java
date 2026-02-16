package com.raisedeveloper.server.domain.auth.api;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;
import static com.raisedeveloper.server.global.validation.RegexPatterns.*;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.auth.application.AuthService;
import com.raisedeveloper.server.domain.auth.dto.AuthFcmRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthFcmResponse;
import com.raisedeveloper.server.domain.auth.dto.AuthLoginRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthLoginResponse;
import com.raisedeveloper.server.domain.auth.dto.AuthLogoutResponse;
import com.raisedeveloper.server.domain.auth.dto.AuthRefreshRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthRefreshResponse;
import com.raisedeveloper.server.domain.auth.dto.AuthSignUpRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthSignUpResponse;
import com.raisedeveloper.server.domain.auth.dto.AvailabilityResponse;
import com.raisedeveloper.server.global.response.ApiResponse;
import com.raisedeveloper.server.global.security.utils.AuthUtils;

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
		return ApiResponse.of("AUTH_SIGNUP_SUCCESS", authService.signup(request));
	}

	@PostMapping("/login")
	public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
		return ApiResponse.of("AUTH_LOGIN_SUCCESS", authService.login(request));
	}

	@PostMapping("/refresh")
	public ApiResponse<AuthRefreshResponse> refresh(@Valid @RequestBody AuthRefreshRequest request) {
		return ApiResponse.of("AUTH_REFRESH_SUCCESS", authService.refresh(request));
	}

	@PostMapping("/logout")
	public ApiResponse<AuthLogoutResponse> logout() {
		Long userId = AuthUtils.resolveUserIdFromContext();
		authService.logoutAll(userId);
		return ApiResponse.of("LOGOUT_SUCCESS", new AuthLogoutResponse(true));
	}

	@PutMapping("/fcm")
	public ApiResponse<AuthFcmResponse> setFcmToken(@Valid @RequestBody AuthFcmRequest request) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		authService.setFcmToken(userId, request.fcmToken());
		return ApiResponse.of("SET_FCM_TOKEN_SUCCESS", new AuthFcmResponse());
	}

	@GetMapping("/email-availability")
	public ApiResponse<AvailabilityResponse> checkEmailAvailability(
		@Email(message = AUTH_EMAIL_FORMAT_INVALID_MESSAGE)
		@NotBlank(message = AUTH_EMAIL_REQUIRED_MESSAGE)
		@RequestParam("email") String email
	) {
		return ApiResponse.of(
			"USER_EMAIL_AVAILABLE",
			new AvailabilityResponse(authService.isEmailAvailable(email))
		);
	}

	@GetMapping("/nickname-availability")
	public ApiResponse<AvailabilityResponse> checkNicknameAvailability(
		@NotBlank(message = USER_NICKNAME_REQUIRED_MESSAGE)
		@Size(max = 10, message = USER_NICKNAME_LENGTH_INVALID_MESSAGE)
		@Pattern(regexp = NICKNAME_REGEX, message = USER_NICKNAME_FORMAT_INVALID_MESSAGE)
		@RequestParam("nickname") String nickname
	) {
		return ApiResponse.of(
			"USER_NICKNAME_AVAILABLE",
			new AvailabilityResponse(authService.isNicknameAvailable(nickname))
		);
	}

}
