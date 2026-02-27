package com.raisedeveloper.server.domain.exercise.event;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.global.outbox.application.OutboxEventStore;
import com.raisedeveloper.server.global.outbox.domain.OutboxAggregateType;
import com.raisedeveloper.server.global.outbox.domain.OutboxEventType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExerciseSessionEventPublisher {

	private final OutboxEventStore outboxEventStore;

	public void publishSessionCompleted(ExerciseSessionCompletedEvent event) {
		outboxEventStore.store(
			event.eventId(),
			OutboxAggregateType.EXERCISE_SESSION,
			event.sessionId().toString(),
			ExerciseKafkaTopics.SESSION_COMPLETED,
			OutboxEventType.EXERCISE_SESSION_COMPLETED,
			String.valueOf(event.userId()),
			event
		);
	}
}
