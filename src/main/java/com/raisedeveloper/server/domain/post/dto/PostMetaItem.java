package com.raisedeveloper.server.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostMetaItem(
	Long postId,
	PostAuthor author,
	int likeCount,
	boolean isLiked,
	Boolean isAuthor
) {
}
