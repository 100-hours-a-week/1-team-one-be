package com.raisedeveloper.server.domain.user.dto;

import static com.raisedeveloper.server.domain.common.ValidationConstants.*;

import jakarta.validation.constraints.NotBlank;

public record ProfileImageUpdateRequest(
	@NotBlank(message = IMAGE_PATH_REQUIRED)
	String imagePath
) {
}
