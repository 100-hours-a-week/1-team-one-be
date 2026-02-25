package com.raisedeveloper.server.global.exception;

public final class ErrorMessageConstants {

	private ErrorMessageConstants() {
	}

	// Common
	public static final String COMMON_INVALID_JSON_MESSAGE = "요청 본문이 올바른 JSON 형식이 아닙니다.";
	public static final String COMMON_METHOD_NOT_ALLOWED_MESSAGE = "허용되지 않은 HTTP 메서드입니다.";
	public static final String COMMON_ACCESS_DENIED_MESSAGE = "접근 권한이 없습니다.";
	public static final String COMMON_VALIDATION_FAILED_MESSAGE = "요청 값 검증에 실패했습니다.";
	public static final String COMMON_INTERNAL_SERVER_ERROR_MESSAGE = "서버 내부 오류가 발생했습니다.";
	public static final String COMMON_RESOURCE_NOT_FOUND_MESSAGE = "요청한 리소스를 찾을 수 없습니다.";
	public static final String COMMON_UNSUPPORTED_MEDIA_TYPE_MESSAGE = "지원하지 않는 Content-Type 입니다.";

	// JWT / Auth (error reasons)
	public static final String AUTH_ACCESS_TOKEN_MISSING_MESSAGE = "인증 토큰이 없습니다.";
	public static final String AUTH_ACCESS_TOKEN_EXPIRED_MESSAGE = "액세스 토큰이 만료되었습니다.";
	public static final String AUTH_ACCESS_TOKEN_INVALID_MESSAGE = "액세스 토큰이 유효하지 않습니다.";
	public static final String AUTH_REFRESH_TOKEN_EXPIRED_MESSAGE = "리프레시 토큰이 만료되었습니다.";
	public static final String AUTH_REFRESH_TOKEN_INVALID_MESSAGE = "리프레시 토큰이 유효하지 않습니다.";
	public static final String AUTH_INVALID_CREDENTIALS_MESSAGE = "이메일 또는 비밀번호가 올바르지 않습니다.";

	// User domain (error reasons)
	public static final String USER_NOT_FOUND_MESSAGE = "사용자를 찾을 수 없습니다.";
	public static final String ALARM_SETTING_NOT_FOUND_MESSAGE = "알람 설정 정보를 찾을 수 없습니다.";
	public static final String CHARACTER_NOT_SET_MESSAGE = "캐릭터가 설정되어 있지 않습니다.";
	public static final String USER_EMAIL_DUPLICATED_MESSAGE = "이미 사용 중인 이메일입니다.";
	public static final String USER_NICKNAME_DUPLICATED_MESSAGE = "이미 사용 중인 닉네임입니다.";
	public static final String CHARACTER_ALREADY_SET_MESSAGE = "이미 캐릭터가 설정되어 있습니다.";

	// Survey domain (error reasons)
	public static final String SURVEY_NOT_FOUND_MESSAGE = "설문을 찾을 수 없습니다.";

	// Routine domain (error reasons)
	public static final String ROUTINE_NOT_FOUND_MESSAGE = "루틴을 찾을 수 없습니다.";

	// Image domain (error reasons)
	public static final String INVALID_FILE_EXTENSION_MESSAGE = "허용되지 않은 파일 확장자입니다.";
	public static final String INVALID_FILE_PATH_MESSAGE = "잘못된 파일 경로입니다.";
	public static final String IMAGE_NOT_FOUND_MESSAGE = "이미지를 찾을 수 없습니다.";
	public static final String IMAGE_UPLOAD_FAILED_MESSAGE = "이미지 업로드에 실패했습니다.";
	public static final String IMAGE_DELETE_FAILED_MESSAGE = "이미지 삭제에 실패했습니다.";
	public static final String PRESIGNED_URL_GENERATION_FAILED_MESSAGE = "업로드 URL 생성에 실패했습니다.";

	// Exercise domain (error reasons)
	public static final String EXERCISE_NOT_FOUND_MESSAGE = "운동 정보를 찾을 수 없습니다.";
	public static final String EXERCISE_TYPE_MISMATCH_MESSAGE = "운동 타입이 일치하지 않습니다.";
	public static final String EXERCISE_SESSION_NOT_FOUND_MESSAGE = "운동 세션을 찾을 수 없습니다.";

	// AI server (error reasons)
	public static final String AI_SERVER_CONNECTION_FAILED_MESSAGE = "AI 서버에 연결할 수 없습니다.";
	public static final String AI_SERVER_TIMEOUT_MESSAGE = "AI 서버 응답 시간이 초과되었습니다.";
	public static final String AI_RESPONSE_PARSE_FAILED_MESSAGE = "AI 서버 응답 역직렬화에 실패했습니다.";
	public static final String AI_SERVER_ERROR_MESSAGE = "AI 서버에서 오류가 발생했습니다.";
	public static final String AI_ROUTINE_NOT_COMPLETED_MESSAGE = "AI 루틴 생성이 완료되지 않았습니다.";
	public static final String AI_ROUTINE_EMPTY_MESSAGE = "AI가 루틴을 생성하지 못했습니다.";
	public static final String AI_ROUTINE_STEPS_EMPTY_MESSAGE = "루틴에 운동 단계가 없습니다.";
	public static final String AI_ROUTINE_EXERCISE_ID_MISSING_MESSAGE = "운동 ID가 누락되었습니다.";
	public static final String AI_ROUTINE_TARGET_REPS_MISSING_MESSAGE = "목표 반복 횟수가 누락되었습니다.";
	public static final String AI_ROUTINE_DURATION_TIME_MISSING_MESSAGE = "운동 지속 시간이 누락되었습니다.";
	public static final String AI_ROUTINE_EYES_INVALID_MESSAGE = "눈 운동 루틴 데이터에 이상이 발생했습니다.";
	public static final String AI_ROUTINE_GENERATION_FAILED_MESSAGE = "AI 루틴 생성에 실패했습니다.";

	// Post Domain (error reasons)
	public static final String POST_NOT_FOUND_MESSAGE = "해당 게시글을 찾을 수 없습니다.";

	// Validation: Auth
	public static final String AUTH_EMAIL_FORMAT_INVALID_MESSAGE = "올바른 이메일 형식을 입력해주세요.";
	public static final String AUTH_EMAIL_REQUIRED_MESSAGE = "이메일은 필수입니다.";
	public static final String AUTH_EMAIL_TOO_LONG_MESSAGE = "이메일은 최대 255자까지 가능합니다.";
	public static final String AUTH_PASSWORD_LENGTH_INVALID_MESSAGE = "비밀번호는 8자 이상 16자 이하여야 합니다.";
	public static final String AUTH_PASSWORD_REQUIRED_MESSAGE = "비밀번호는 필수 값입니다.";
	public static final String AUTH_PASSWORD_FORMAT_INVALID_MESSAGE = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.";
	public static final String AUTH_REFRESH_TOKEN_REQUIRED_MESSAGE = "리프레시 토큰은 필수입니다.";
	public static final String AUTH_FCM_TOKEN_INVALID_MESSAGE = "FCM 토큰이 유효하지 않습니다.";

	// Validation: User
	public static final String USER_NICKNAME_REQUIRED_MESSAGE = "닉네임은 필수 값입니다.";
	public static final String USER_NICKNAME_LENGTH_INVALID_MESSAGE = "닉네임은 10자 이내로 입력해주세요.";
	public static final String USER_NICKNAME_FORMAT_INVALID_MESSAGE = "닉네임은 한글/영문만 입력 가능하며 공백 및 특수문자를 사용할 수 없습니다.";
	public static final String USER_PROFILE_IMAGE_PATH_REQUIRED_MESSAGE = "이미지 경로는 필수 값입니다.";
	public static final String USER_CHARACTER_TYPE_REQUIRED_MESSAGE = "캐릭터 타입은 필수입니다.";
	public static final String USER_ALARM_INTERVAL_REQUIRED_MESSAGE = "알람 간격은 필수입니다.";
	public static final String USER_ALARM_INTERVAL_MIN_MESSAGE = "알람 간격은 최소 10분이어야 합니다.";
	public static final String USER_ALARM_INTERVAL_MAX_MESSAGE = "알람 간격은 최대 600분까지 가능합니다.";
	public static final String USER_ALARM_ACTIVE_START_REQUIRED_MESSAGE = "알람 활성 시작 시간은 필수입니다.";
	public static final String USER_ALARM_ACTIVE_END_REQUIRED_MESSAGE = "알람 활성 종료 시간은 필수입니다.";
	public static final String USER_ALARM_REPEAT_DAYS_REQUIRED_MESSAGE = "반복 요일은 최소 1개 이상이어야 합니다.";
	public static final String USER_ALARM_REPEAT_DAY_INVALID_MESSAGE = "반복 요일 값이 올바르지 않습니다.";
	public static final String USER_DND_FINISHED_AT_REQUIRED_MESSAGE = "방해금지 종료 시간은 필수입니다.";

	// Validation: Image
	public static final String IMAGE_FILE_NAME_REQUIRED_MESSAGE = "파일명은 필수입니다.";
	public static final String IMAGE_CONTENT_TYPE_REQUIRED_MESSAGE = "컨텐츠 타입은 필수입니다.";

	// Validation: Notification
	public static final String NOTIFICATION_OLDEST_ID_REQUIRED_MESSAGE = "가장 오래된 알림 ID는 필수입니다.";
	public static final String NOTIFICATION_LATEST_ID_REQUIRED_MESSAGE = "가장 최신 알림 ID는 필수입니다.";

	// Validation: Survey
	public static final String SURVEY_ID_REQUIRED_MESSAGE = "설문 ID는 필수입니다.";
	public static final String SURVEY_RESPONSES_REQUIRED_MESSAGE = "설문 응답은 비어 있을 수 없습니다.";
	public static final String SURVEY_QUESTION_ID_REQUIRED_MESSAGE = "질문 ID는 필수입니다.";
	public static final String SURVEY_OPTION_ID_REQUIRED_MESSAGE = "선택지 ID는 필수입니다.";

	// Validation: Exercise
	public static final String EXERCISE_ROUTINE_STEP_ID_REQUIRED_MESSAGE = "루틴 단계 ID는 필수입니다.";
	public static final String EXERCISE_RESULT_STATUS_REQUIRED_MESSAGE = "운동 결과 상태는 필수입니다.";
	public static final String EXERCISE_POSE_RECORD_REQUIRED_MESSAGE = "포즈 기록은 필수입니다.";
	public static final String EXERCISE_ACCURACY_REQUIRED_MESSAGE = "정확도는 필수입니다.";
	public static final String EXERCISE_ACCURACY_MIN_MESSAGE = "정확도는 최소 0 이상이어야 합니다.";
	public static final String EXERCISE_ACCURACY_MAX_MESSAGE = "정확도는 최대 100 이하여야 합니다.";
	public static final String EXERCISE_START_AT_REQUIRED_MESSAGE = "운동 시작 시간은 필수입니다.";
	public static final String EXERCISE_END_AT_REQUIRED_MESSAGE = "운동 종료 시간은 필수입니다.";
	public static final String EXERCISE_SESSION_START_AT_REQUIRED_MESSAGE = "세션 시작 시간은 필수입니다.";
	public static final String EXERCISE_SESSION_END_AT_REQUIRED_MESSAGE = "세션 종료 시간은 필수입니다.";
	public static final String EXERCISE_SESSION_RESULTS_REQUIRED_MESSAGE = "운동 결과는 필수입니다.";

	// Validation: Post Domain
	public static final String POST_IMAGES_TOO_MANY_MESSAGE =
		"하나의 게시글에 이미지는 최대 10개까지 포함할 수 있습니다.";
	public static final String POST_TITLE_BLANK_MESSAGE = "게시글 제목은 비어 있을 수 없습니다.";
	public static final String POST_TITLE_TOO_LONG_MESSAGE = "게시글 제목은 최대 50자까지 가능합니다.";
	public static final String POST_CONTENT_BLANK_MESSAGE = "게시글 내용은 비어 있을 수 없습니다.";
	public static final String POST_CONTENT_TOO_LONG_MESSAGE = "게시글 내용은 최대 500자까지 가능합니다.";
	public static final String POST_IMAGE_PATH_BLANK_MESSAGE = "이미지 경로는 비어 있을 수 없습니다.";
	public static final String POST_TAGS_TOO_MANY_MESSAGE = "태그는 최대 5개까지 가능합니다.";
	public static final String POST_TAG_NAME_BLANK_MESSAGE = "태그 이름은 비어 있을 수 없습니다.";
	public static final String POST_TAG_NAME_TOO_LONG_MESSAGE = "태그 이름은 최대 10자까지 가능합니다.";
	public static final String POST_LIKED_REQUIRED_MESSAGE = "좋아요 상태는 필수입니다.";
}
