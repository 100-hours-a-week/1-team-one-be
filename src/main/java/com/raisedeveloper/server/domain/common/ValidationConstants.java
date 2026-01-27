package com.raisedeveloper.server.domain.common;

public final class ValidationConstants {

	public static final String EMAIL_FORMAT_INVALID = "올바른 이메일 형식을 입력해주세요.";
	public static final String EMAIL_REQUIRED = "이메일은 필수입니다.";

	public static final String PASSWORD_LENGTH_INVALID = "비밀번호는 최소 6자 이상이어야 합니다.";
	public static final String PASSWORD_REQUIRED = "비밀번호는 필수 값입니다.";
	public static final String PASSWORD_FORMAT_INVALID = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.";

	public static final String NICKNAME_REQUIRED = "닉네임은 필수 값입니다.";
	public static final String NICKNAME_LENGTH_INVALID = "닉네임은 10자 이내로 입력해주세요.";
	public static final String NICKNAME_FORMAT_INVALID = "닉네임은 한글/영문만 입력 가능하며 공백 및 특수문자를 사용할 수 없습니다.";

	public static final String FCM_TOKEN_INVALID = "FCM 토큰이 유효하지 않습니다.";

	public static final String IMAGE_PATH_REQUIRED = "이미지 경로는 필수 값입니다.";
}
