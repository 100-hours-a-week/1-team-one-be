package com.raisedeveloper.server.global.outbox.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OutboxAggregateType {
	EXERCISE_SESSION("EXERCISE_SESSION"),
	USER_CHARACTER("USER_CHARACTER"),
	PUSH_DELIVERY("PUSH_DELIVERY");

	private final String value;
}
