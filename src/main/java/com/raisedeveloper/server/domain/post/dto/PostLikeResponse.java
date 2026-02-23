package com.raisedeveloper.server.domain.post.dto;

public record PostLikeResponse(
	Long postId,
	boolean isLiked
) {
}
