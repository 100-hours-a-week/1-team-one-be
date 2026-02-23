package com.raisedeveloper.server.domain.notification.dto;

import java.util.List;

import com.raisedeveloper.server.global.pagination.PagingResponse;

public record NotificationListResponse(
	List<NotificationItemResponse> notifications,
	PagingResponse paging
) {
}
