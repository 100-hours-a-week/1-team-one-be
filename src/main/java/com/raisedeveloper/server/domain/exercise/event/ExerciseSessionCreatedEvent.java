package com.raisedeveloper.server.domain.exercise.event;

import java.time.LocalDateTime;

public record ExerciseSessionCreatedEvent(
	String eventId,
	String sourceEventId,
	Long userId,
	Long sessionId,
	LocalDateTime scheduledAt,
	LocalDateTime createdAt
) {
}
