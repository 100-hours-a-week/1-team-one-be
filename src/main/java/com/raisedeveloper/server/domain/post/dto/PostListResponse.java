package com.raisedeveloper.server.domain.post.dto;

import java.util.List;

import com.raisedeveloper.server.global.pagination.PagingResponse;

public record PostListResponse(
	List<PostListItem> posts,
	PagingResponse paging
) {
}
