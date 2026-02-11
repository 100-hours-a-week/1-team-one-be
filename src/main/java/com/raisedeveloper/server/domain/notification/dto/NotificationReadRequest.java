package com.raisedeveloper.server.domain.notification.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record NotificationReadRequest(
	@NotNull(message = NOTIFICATION_LAST_TIME_REQUIRED)
	LocalDateTime lastNotificationTime
) {
}
