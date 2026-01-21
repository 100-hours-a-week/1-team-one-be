package com.raisedeveloper.server.domain.auth.dto;

import com.raisedeveloper.server.domain.user.domain.User;

public record AuthSignUpResponse(
	Long userId
) {
	public static AuthSignUpResponse from(User user) {
		return new AuthSignUpResponse(user.getId());
	}
}
