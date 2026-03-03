package com.raisedeveloper.server.domain.post.application;

public record PostLikeChangedEvent(
	Long postId,
	int delta
) {
}
