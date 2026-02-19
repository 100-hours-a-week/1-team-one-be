package com.raisedeveloper.server.global.security.currentuser;

import java.security.Principal;

import com.raisedeveloper.server.domain.common.enums.Role;
import com.raisedeveloper.server.global.security.jwt.TokenType;

public record CurrentUserPrincipal(Long userId, String email, Role role, TokenType tokenType) implements Principal {

	@Override
	public String getName() {
		return email;
	}
}
