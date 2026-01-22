package com.raisedeveloper.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// common
	// [400]
	INVALID_JSON(400, "INVALID_JSON", "json format is invalid"),
	USER_NOT_FOUND(404, "NOT_FOUND", "resource not found"),
	VALIDATION_FAILED(422, "VALIDATION_FAILED", "request validation failed"),
	// [500]
	INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "Internal server error"),

	// auth domain
	// [400]
	EXPIRED_TOKEN(401, "JWT_EXPIRED", "Token is expired"),
	INVALID_SIGNATURE(401, "JWT_SIGNATURE_INVALID", "Token signature is invalid"),
	MALFORMED_TOKEN(401, "JWT_MALFORMED", "Token is malformed"),
	UNSUPPORTED_TOKEN(401, "JWT_UNSUPPORTED", "Token type is unsupported"),
	INVALID_TOKEN(401, "JWT_INVALID", "Token is invalid"),
	MISSING_TOKEN(401, "JWT_MISSING", "Authorization token is missing"),

	// user domain,
	AUTH_INVALID_CREDENTIALS(401, "AUTH_INVALID_CREDENTIALS", "invalid email or password"),
	USER_EMAIL_DUPLICATED(409, "USER_EMAIL_DUPLICATED", "email already in use."),
	USER_NICKNAME_DUPLICATED(409, "USER_NICKNAME_DUPLICATED", "nickname already in use."),
	CHARACTER_ALREADY_SET(409, "CHARACTER_ALREADY_SET", "character type is already selected."),
	ALARM_SETTING_NOT_FOUND(404, "ALARM_SETTING_NOT_FOUND", "alarm setting data not found");

	private final int httpStatusCode;
	private final String code;
	private final String reason;

	public boolean isJwtError() {
		return code != null && code.startsWith("JWT_");
	}
}
