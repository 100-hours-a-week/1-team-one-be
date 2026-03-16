package com.raisedeveloper.server.global.pagination;

public record BiDirectionPagingResponse(
	String prevCursor,
	String nextCursor,
	boolean hasPrev,
	boolean hasNext
) {
}
