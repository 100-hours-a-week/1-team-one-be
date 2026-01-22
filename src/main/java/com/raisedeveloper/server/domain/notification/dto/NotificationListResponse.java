package com.raisedeveloper.server.domain.notification.dto;

import java.util.List;

public record NotificationListResponse(
	List<NotificationItemResponse> notifications,
	NotificationPagingResponse paging
) {
}
