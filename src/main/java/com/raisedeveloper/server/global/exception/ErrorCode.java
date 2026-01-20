package com.raisedeveloper.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// common
	// [500]
	INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "Internal server error"),

	// auth domain
	// [400]
	EXPIRED_TOKEN(401, "JWT_EXPIRED", "Token is expired"),
	INVALID_SIGNATURE(401, "JWT_SIGNATURE_INVALID", "Token signature is invalid"),
	MALFORMED_TOKEN(401, "JWT_MALFORMED", "Token is malformed"),
	UNSUPPORTED_TOKEN(401, "JWT_UNSUPPORTED", "Token type is unsupported"),
	INVALID_TOKEN(401, "JWT_INVALID", "Token is invalid"),
	MISSING_TOKEN(401, "JWT_MISSING", "Authorization token is missing");

	private final int httpStatusCode;
	private final String code;
	private final String reason;
}
