package com.raisedeveloper.server.domain.notification.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

public record NotificationReadRequest(
	@NotNull
	LocalDateTime lastNotificationTime
) {
}
