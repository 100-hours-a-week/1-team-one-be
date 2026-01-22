package com.raisedeveloper.server.domain.notification.dto;

public record NotificationPagingResponse(
	String nextCursor,
	boolean hasNext
) {
}
