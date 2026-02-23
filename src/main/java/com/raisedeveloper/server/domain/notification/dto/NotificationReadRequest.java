package com.raisedeveloper.server.domain.notification.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import jakarta.validation.constraints.NotNull;

public record NotificationReadRequest(
	@NotNull(message = NOTIFICATION_OLDEST_ID_REQUIRED_MESSAGE)
	Long oldestNotificationId,
	@NotNull(message = NOTIFICATION_LATEST_ID_REQUIRED_MESSAGE)
	Long latestNotificationId
) {
}
