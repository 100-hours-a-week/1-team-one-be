package com.raisedeveloper.server.global.security.jwt;

import java.time.LocalDateTime;

public record TokenResult(
	String token,
	LocalDateTime expiresAt
) {
}
