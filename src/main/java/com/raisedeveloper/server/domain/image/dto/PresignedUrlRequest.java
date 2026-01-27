package com.raisedeveloper.server.domain.image.dto;

import static com.raisedeveloper.server.domain.common.ValidationConstants.*;

import jakarta.validation.constraints.NotBlank;

public record PresignedUrlRequest(
	@NotBlank(message = FILE_NAME_REQUIRED)
	String fileName,

	@NotBlank(message = CONTENT_TYPE_REQUIRED)
	String contentType
) {
}
