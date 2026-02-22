package com.raisedeveloper.server.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostListItem(
	Long postId,
	PostAuthor author,
	String title,
	String content,
	String imageUrl,
	List<PostTagInfo> tags,
	int likeCount,
	LocalDateTime createdAt,
	boolean isLiked
) {
}
