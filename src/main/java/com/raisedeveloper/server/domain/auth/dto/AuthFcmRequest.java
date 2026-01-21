package com.raisedeveloper.server.domain.auth.dto;

import static com.raisedeveloper.server.domain.common.ValidationConstants.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthFcmRequest(
	@NotBlank(message = FCM_TOKEN_INVALID)
	@Size(max = 255, message = FCM_TOKEN_INVALID)
	String fcmToken
) {
}
