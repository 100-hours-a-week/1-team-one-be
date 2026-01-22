package com.raisedeveloper.server.domain.user.dto;

import static com.raisedeveloper.server.domain.common.ValidationConstants.*;
import static com.raisedeveloper.server.global.validation.RegexPatterns.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileNicknameUpdateRequest(
	@NotBlank(message = NICKNAME_REQUIRED)
	@Pattern(regexp = NICKNAME_REGEX, message = NICKNAME_FORMAT_INVALID)
	@Size(max = 10, message = NICKNAME_LENGTH_INVALID)
	String nickname
) {
}
