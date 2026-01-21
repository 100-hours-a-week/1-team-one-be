package com.raisedeveloper.server.domain.auth.dto;

import com.raisedeveloper.server.domain.user.domain.User;

public record AuthLoginResponse(
	Tokens tokens,
	Long userId,
	boolean isOnboardingCompleted
) {
	public static AuthLoginResponse from(Tokens tokens, User user) {
		return new AuthLoginResponse(
			tokens,
			user.getId(),
			user.isOnboardingCompleted()
		);
	}
}
