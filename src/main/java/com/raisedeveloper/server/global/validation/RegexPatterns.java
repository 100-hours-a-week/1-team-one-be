package com.raisedeveloper.server.global.validation;

public final class RegexPatterns {
	private RegexPatterns() {
	}

	public static final String NICKNAME_REGEX = "^[A-Za-z가-힣]+$";
	public static final String REPEAT_DAY_REGEX = "^(MON|TUE|WED|THU|FRI)$";
}
