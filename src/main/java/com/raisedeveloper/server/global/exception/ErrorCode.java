package com.raisedeveloper.server.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// common
	// [400]
	INVALID_JSON(400, "INVALID_JSON", "json format is invalid"),
	// [405]
	METHOD_NOT_ALLOWED(405, "METHOD_NOT_ALLOWED", "method not allowed"),
	ACCESS_DENIED(403, "ACCESS_DENIED", "access denied"),
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

	// user domain
	// [400]
	AUTH_INVALID_CREDENTIALS(401, "AUTH_INVALID_CREDENTIALS", "invalid email or password"),

	ALARM_SETTING_NOT_FOUND(404, "ALARM_SETTING_NOT_FOUND", "alarm setting data not found"),

	CHARACTER_NOT_SET(409, "CHARACTER_NOT_FOUND", "character is not set"),
	USER_EMAIL_DUPLICATED(409, "USER_EMAIL_DUPLICATED", "email already in use."),
	USER_NICKNAME_DUPLICATED(409, "USER_NICKNAME_DUPLICATED", "nickname already in use."),
	CHARACTER_ALREADY_SET(409, "CHARACTER_ALREADY_SET", "character type is already selected."),

	// survey domain
	SURVEY_NOT_FOUND(404, "SURVEY_NOT_FOUND", "survey not found"),

	// routine domain
	ROUTINE_NOT_FOUND(404, "ROUTINE_NOT_FOUND", "routine not found"),

	// image domain
	// [400]
	INVALID_FILE_EXTENSION(400, "INVALID_FILE_EXTENSION", "허용되지 않은 파일 확장자입니다."),
	INVALID_FILE_PATH(400, "INVALID_FILE_PATH", "잘못된 파일 경로입니다."),
	// [404]
	IMAGE_NOT_FOUND(404, "IMAGE_NOT_FOUND", "이미지를 찾을 수 없습니다."),
	// [500]
	IMAGE_UPLOAD_FAILED(500, "IMAGE_UPLOAD_FAILED", "이미지 업로드에 실패했습니다."),
	IMAGE_DELETE_FAILED(500, "IMAGE_DELETE_FAILED", "이미지 삭제에 실패했습니다."),
	PRESIGNED_URL_GENERATION_FAILED(500, "PRESIGNED_URL_GENERATION_FAILED", "업로드 URL 생성에 실패했습니다.");

	private final int httpStatusCode;
	private final String code;
	private final String reason;

	public boolean isJwtError() {
		return code != null && code.startsWith("JWT_");
	}
}
