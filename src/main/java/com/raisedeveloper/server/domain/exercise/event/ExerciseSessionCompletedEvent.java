package com.raisedeveloper.server.domain.exercise.event;

import java.time.LocalDateTime;

public record ExerciseSessionCompletedEvent(
	String eventId,
	LocalDateTime occurredAt,
	Long sessionId,
	Long userId,
	Long routineId,
	LocalDateTime startAt,
	LocalDateTime endAt,
	boolean routineCompleted,
	long completedCount,
	boolean firstCompletionToday
) {
}
