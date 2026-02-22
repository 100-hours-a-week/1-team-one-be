package com.raisedeveloper.server.domain.auth.dto;

public record AuthLoginResponse(
	Tokens tokens,
	Long userId,
	boolean isOnboardingCompleted
) {
}
