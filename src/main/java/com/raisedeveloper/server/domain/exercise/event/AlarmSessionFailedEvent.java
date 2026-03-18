package com.raisedeveloper.server.domain.exercise.event;

import java.time.LocalDateTime;

public record AlarmSessionFailedEvent(
	String eventId,
	String sourceEventId,
	Long userId,
	LocalDateTime scheduledAt,
	String reason,
	LocalDateTime failedAt
) {
}
