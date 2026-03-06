package com.raisedeveloper.server.domain.exercise.event;

import java.time.LocalDateTime;

public record ExerciseSessionPushResultEvent(
	String eventId,
	String sourceEventId,
	Long userId,
	Long sessionId,
	String status,
	LocalDateTime processedAt
) {
}
