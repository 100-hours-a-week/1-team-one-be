package com.raisedeveloper.server.domain.auth.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthFcmRequest(
	@NotBlank(message = AUTH_FCM_TOKEN_INVALID_MESSAGE)
	@Size(max = 255, message = AUTH_FCM_TOKEN_INVALID_MESSAGE)
	String fcmToken
) {
}
