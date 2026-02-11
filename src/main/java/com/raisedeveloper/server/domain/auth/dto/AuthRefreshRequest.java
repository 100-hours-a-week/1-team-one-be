package com.raisedeveloper.server.domain.auth.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import jakarta.validation.constraints.NotBlank;

public record AuthRefreshRequest(
	@NotBlank(message = AUTH_REFRESH_TOKEN_REQUIRED)
	String refreshToken
) {
}
