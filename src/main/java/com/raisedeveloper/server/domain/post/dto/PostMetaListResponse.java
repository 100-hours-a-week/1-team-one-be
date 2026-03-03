package com.raisedeveloper.server.domain.post.dto;

import java.util.List;

import com.raisedeveloper.server.global.pagination.PagingResponse;

public record PostMetaListResponse(
	List<PostMetaItem> posts,
	PagingResponse paging
) {
}
