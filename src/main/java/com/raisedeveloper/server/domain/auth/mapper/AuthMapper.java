package com.raisedeveloper.server.domain.auth.mapper;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.auth.dto.AuthLoginResponse;
import com.raisedeveloper.server.domain.auth.dto.AuthSignUpResponse;
import com.raisedeveloper.server.domain.auth.dto.Tokens;
import com.raisedeveloper.server.domain.user.domain.User;

@Component
public class AuthMapper {

	public AuthSignUpResponse toSignUpResponse(User user) {
		return new AuthSignUpResponse(user.getId());
	}

	public AuthLoginResponse toLoginResponse(Tokens tokens, User user) {
		return new AuthLoginResponse(
			tokens,
			user.getId(),
			user.isOnboardingCompleted()
		);
	}
}
