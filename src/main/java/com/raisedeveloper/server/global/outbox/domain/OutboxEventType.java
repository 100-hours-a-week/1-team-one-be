package com.raisedeveloper.server.global.outbox.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OutboxEventType {
	EXERCISE_SESSION_COMPLETED("ExerciseSessionCompletedEvent"),
	EXERCISE_SESSION_REWARD_APPLIED("ExerciseSessionRewardAppliedEvent");

	private final String value;
}
