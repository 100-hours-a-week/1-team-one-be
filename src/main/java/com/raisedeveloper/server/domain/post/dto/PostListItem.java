package com.raisedeveloper.server.domain.post.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.raisedeveloper.server.domain.post.domain.Post;

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
	public static PostListItem from(Post post, PostAuthor author, List<PostTagInfo> tags) {
		return new PostListItem(
			post.getId(),
			author,
			post.getTitle(),
			post.getContent(),
			post.getThumbnailImagePath(),
			tags,
			post.getLikeCount(),
			post.getCreatedAt(),
			false
		);
	}
}
