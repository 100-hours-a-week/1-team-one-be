package com.raisedeveloper.server.domain.user.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;
import static com.raisedeveloper.server.global.validation.RegexPatterns.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileNicknameUpdateRequest(
	@NotBlank(message = USER_NICKNAME_REQUIRED_MESSAGE)
	@Pattern(regexp = NICKNAME_REGEX, message = USER_NICKNAME_FORMAT_INVALID_MESSAGE)
	@Size(max = 10, message = USER_NICKNAME_LENGTH_INVALID_MESSAGE)
	String nickname
) {
}
