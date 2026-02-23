package com.raisedeveloper.server.domain.image.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import jakarta.validation.constraints.NotBlank;

public record PresignedUrlRequest(
	@NotBlank(message = IMAGE_FILE_NAME_REQUIRED_MESSAGE)
	String fileName,

	@NotBlank(message = IMAGE_CONTENT_TYPE_REQUIRED_MESSAGE)
	String contentType
) {
}
