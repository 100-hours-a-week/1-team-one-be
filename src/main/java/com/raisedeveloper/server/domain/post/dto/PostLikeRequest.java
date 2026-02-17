package com.raisedeveloper.server.domain.post.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import jakarta.validation.constraints.NotNull;

public record PostLikeRequest(
	@NotNull(message = POST_LIKED_REQUIRED_MESSAGE)
	Boolean liked
) {
}
