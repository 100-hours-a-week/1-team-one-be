package com.raisedeveloper.server.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetail(
	Long postId,
	boolean isAuthor,
	PostAuthor author,
	String title,
	String content,
	List<String> images,
	List<PostTagInfo> tags,
	int likeCount,
	LocalDateTime createdAt,
	boolean isLiked
) {
}
