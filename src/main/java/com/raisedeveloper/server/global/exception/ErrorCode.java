package com.raisedeveloper.server.global.exception;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

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
	PRESIGNED_URL_GENERATION_FAILED(500, "PRESIGNED_URL_GENERATION_FAILED", "업로드 URL 생성에 실패했습니다."),

	// exercise domain
	EXERCISE_NOT_FOUND(404, "EXERCISE_NOT_FOUND", "exercise not found"),
	EXERCISE_TYPE_MISMATCH(400, "EXERCISE_TYPE_MISMATCH", "exercise type mismatch"),
	EXERCISE_SESSION_NOT_FOUND(404, "EXERCISE_SESSION_NOT_FOUND", "exercise session not found"),

	// AI server
	AI_SERVER_CONNECTION_FAILED(503, "AI_SERVER_CONNECTION_FAILED", "AI 서버에 연결할 수 없습니다"),
	AI_SERVER_TIMEOUT(504, "AI_SERVER_TIMEOUT", "AI 서버 응답 시간이 초과되었습니다"),
	AI_RESPONSE_PARSE_FAILED(500, "AI_RESPONSE_PARSE_FAILED", "AI 서버 응답 역직렬화 실패"),
	AI_SERVER_ERROR(500, "AI_SERVER_ERROR", "AI 서버에서 오류가 발생했습니다"),
	AI_ROUTINE_NOT_COMPLETED(400, "AI_ROUTINE_NOT_COMPLETED", "AI 루틴 생성이 완료되지 않았습니다"),
	AI_ROUTINE_EMPTY(400, "AI_ROUTINE_EMPTY", "AI가 루틴을 생성하지 못했습니다"),
	AI_ROUTINE_STEPS_EMPTY(400, "AI_ROUTINE_STEPS_EMPTY", "루틴에 운동 단계가 없습니다"),
	AI_ROUTINE_EXERCISE_ID_MISSING(400, "AI_ROUTINE_EXERCISE_ID_MISSING", "운동 ID가 누락되었습니다"),
	AI_ROUTINE_TARGET_REPS_MISSING(400, "AI_ROUTINE_TARGET_REPS_MISSING", "목표 반복 횟수가 누락되었습니다"),
	AI_ROUTINE_DURATION_TIME_MISSING(400, "AI_ROUTINE_DURATION_TIME_MISSING", "운동 지속 시간이 누락되었습니다"),
	AI_ROUTINE_GENERATION_FAILED(500, "AI_ROUTINE_GENERATION_FAILED", "AI 루틴 생성에 실패했습니다"),

	// post domain
	POST_NOT_FOUND(404, "POST_NOT_FOUND", POST_NOT_FOUND_MESSAGE);

	private final int httpStatusCode;
	private final String code;
	private final String reason;

	public boolean isJwtError() {
		return code != null && code.startsWith("JWT_");
	}
}
