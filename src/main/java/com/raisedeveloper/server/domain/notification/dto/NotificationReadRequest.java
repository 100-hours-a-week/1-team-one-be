package com.raisedeveloper.server.domain.notification.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationReadRequest(
	@NotNull
	Long oldestNotificationId,
	@NotNull
	Long latestNotificationId
) {
}
