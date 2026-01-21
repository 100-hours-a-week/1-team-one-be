package com.raisedeveloper.server.domain.auth.dto;

import static com.raisedeveloper.server.domain.common.ValidationConstants.*;
import static com.raisedeveloper.server.domain.common.ValidationConstants.EMAIL_REQUIRED;
import static com.raisedeveloper.server.domain.common.ValidationConstants.PASSWORD_LENGTH_INVALID;
import static com.raisedeveloper.server.domain.common.ValidationConstants.PASSWORD_REQUIRED;

import com.raisedeveloper.server.global.validation.Password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthLoginRequest(
	@Email(message = EMAIL_FORMAT_INVALID)
	@NotBlank(message = EMAIL_REQUIRED)
	@Size(max = 255)
	String email,

	@NotBlank(message = PASSWORD_REQUIRED)
	@Password
	@Size(min = 8, max = 16, message = PASSWORD_LENGTH_INVALID)
	String password
) {
}
