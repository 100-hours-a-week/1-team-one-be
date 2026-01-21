package com.raisedeveloper.server.domain.auth.dto;

import com.raisedeveloper.server.global.security.jwt.TokenResult;

public record Tokens(
	TokenResult accessToken,
	TokenResult refreshToken
) {
}
