package com.raisedeveloper.server.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.raisedeveloper.server.domain.post.domain.Post;

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

	public static PostDetail from(Post post, boolean isAuthor, PostAuthor author, List<String> images,
		List<PostTagInfo> tags) {
		return new PostDetail(
			post.getId(),
			isAuthor,
			author,
			post.getTitle(),
			post.getContent(),
			images,
			tags,
			post.getLikeCount(),
			post.getCreatedAt(),
			false
		);
	}
}
