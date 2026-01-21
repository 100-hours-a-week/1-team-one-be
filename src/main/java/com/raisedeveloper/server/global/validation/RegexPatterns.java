package com.raisedeveloper.server.global.validation;

import java.util.regex.Pattern;

public final class RegexPatterns {
	private RegexPatterns() {
	}

	public static final String NICKNAME_REGEX = "^[A-Za-z가-힣]+$";
	public static final String REPEAT_DAY_REGEX = "^(MON|TUE|WED|THU|FRI)$";
}
