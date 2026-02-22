package com.raisedeveloper.server.domain.post.dto;

public record PostAuthor(
	Long userId,
	String profileImageUrl,
	String nickname,
	int level,
	int streak
) {
}
