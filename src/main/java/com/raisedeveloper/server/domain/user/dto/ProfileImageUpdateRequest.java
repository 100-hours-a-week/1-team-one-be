package com.raisedeveloper.server.domain.user.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import jakarta.validation.constraints.NotBlank;

public record ProfileImageUpdateRequest(
	@NotBlank(message = USER_PROFILE_IMAGE_PATH_REQUIRED)
	String imagePath
) {
}
