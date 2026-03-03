package com.raisedeveloper.server.domain.exercise.event;

import java.time.LocalDateTime;

public record ExerciseSessionRewardAppliedEvent(
	String eventId,
	String sourceEventId,
	LocalDateTime occurredAt,
	Long sessionId,
	Long userId,
	Long routineId,
	LocalDateTime startAt,
	LocalDateTime endAt,
	boolean routineCompleted,
	long completedCount,
	short level,
	int previousExp,
	int earnedExp,
	int streak,
	int previousStatusScore,
	int earnedStatusScore
) {
}
