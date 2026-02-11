package com.raisedeveloper.server.domain.post.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
	@NotBlank(message = POST_TITLE_BLANK_MESSAGE)
	@Size(max = 50, message = POST_TITLE_TOO_LONG_MESSAGE)
	String title,

	@NotBlank(message = POST_CONTENT_BLANK_MESSAGE)
	@Size(max = 500, message = POST_CONTENT_TOO_LONG_MESSAGE)
	String content,

	@Size(max = 10, message = POST_IMAGES_TOO_MANY_MESSAGE)
	List<@NotBlank(message = POST_IMAGE_PATH_BLANK_MESSAGE) String> images,

	@Size(max = 5, message = POST_TAGS_TOO_MANY_MESSAGE)
	List<@NotBlank(message = POST_TAG_NAME_BLANK_MESSAGE)
	@Size(max = 10, message = POST_TAG_NAME_TOO_LONG_MESSAGE) String> tags
) {
}
