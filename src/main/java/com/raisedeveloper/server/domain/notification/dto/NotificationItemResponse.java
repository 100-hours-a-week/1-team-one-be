package com.raisedeveloper.server.domain.notification.dto;

import java.time.LocalDateTime;

public record NotificationItemResponse(
	Long notificationId,
	LocalDateTime createdAt,
	String content,
	String details,
	boolean isRead
) {
}
