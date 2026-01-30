package com.raisedeveloper.server.global.security.jwt;

import com.raisedeveloper.server.domain.common.enums.Role;

public record JwtClaims(
	Long userId,
	String email,
	Role role,
	TokenType tokenType
) {
	public static final String CLAIM_USER_ID = "uid";
	public static final String CLAIM_EMAIL = "email";
	public static final String CLAIM_ROLE = "role";
	public static final String CLAIM_TOKEN_TYPE = "typ";
	public static final String CLAIM_JTI = "jti";
}
