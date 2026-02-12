package com.raisedeveloper.server.domain.auth.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import com.raisedeveloper.server.global.validation.Password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthLoginRequest(
	@Email(message = AUTH_EMAIL_FORMAT_INVALID_MESSAGE)
	@NotBlank(message = AUTH_EMAIL_REQUIRED_MESSAGE)
	@Size(max = 255, message = AUTH_EMAIL_TOO_LONG_MESSAGE)
	String email,

	@NotBlank(message = AUTH_PASSWORD_REQUIRED_MESSAGE)
	@Password
	@Size(min = 8, max = 16, message = AUTH_PASSWORD_LENGTH_INVALID_MESSAGE)
	String password
) {
}
