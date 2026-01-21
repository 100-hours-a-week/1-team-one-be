package com.raisedeveloper.server.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRefreshRequest(
	@NotBlank
	String refreshToken
) {
}
