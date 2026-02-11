package com.raisedeveloper.server.domain.auth.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;
import static com.raisedeveloper.server.global.validation.RegexPatterns.*;

import com.raisedeveloper.server.global.validation.Password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthSignUpRequest(
	@Email(message = AUTH_EMAIL_FORMAT_INVALID)
	@NotBlank(message = AUTH_EMAIL_REQUIRED)
	@Size(max = 255, message = AUTH_EMAIL_TOO_LONG)
	String email,

	@NotBlank(message = AUTH_PASSWORD_REQUIRED)
	@Password
	@Size(min = 8, max = 16, message = AUTH_PASSWORD_LENGTH_INVALID)
	String password,

	@NotBlank(message = USER_NICKNAME_REQUIRED)
	@Pattern(regexp = NICKNAME_REGEX, message = USER_NICKNAME_FORMAT_INVALID)
	@Size(max = 10, message = USER_NICKNAME_LENGTH_INVALID)
	String nickname,

	@NotBlank(message = USER_PROFILE_IMAGE_PATH_REQUIRED)
	String imagePath
) {
}
