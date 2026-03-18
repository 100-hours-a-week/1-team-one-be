package com.raisedeveloper.server.domain.exercise.event;

import java.time.LocalDateTime;

public record AlarmDueUserEvent(
	String eventId,
	Long userId,
	LocalDateTime scheduledAt,
	LocalDateTime producedAt,
	String traceId
) {
}
