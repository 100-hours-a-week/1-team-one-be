package com.raisedeveloper.server.global.pagination;

public record PagingResponse(
	String nextCursor,
	boolean hasNext
) {
}
