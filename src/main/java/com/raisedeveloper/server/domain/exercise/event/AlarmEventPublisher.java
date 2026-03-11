package com.raisedeveloper.server.domain.exercise.event;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.global.outbox.application.OutboxEventStore;
import com.raisedeveloper.server.global.outbox.domain.OutboxAggregateType;
import com.raisedeveloper.server.global.outbox.domain.OutboxEventType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AlarmEventPublisher {

	private final OutboxEventStore outboxEventStore;

	public void publishSessionCreated(ExerciseSessionCreatedEvent event) {
		outboxEventStore.store(
			event.eventId(),
			OutboxAggregateType.EXERCISE_SESSION,
			event.sessionId().toString(),
			ExerciseKafkaTopics.ALARM_SESSION_CREATED_V1,
			OutboxEventType.ALARM_SESSION_CREATED,
			event.userId().toString(),
			event
		);
	}

	public void publishPushResult(ExerciseSessionPushResultEvent event) {
		outboxEventStore.store(
			event.eventId(),
			OutboxAggregateType.PUSH_DELIVERY,
			event.sessionId().toString(),
			ExerciseKafkaTopics.ALARM_PUSH_RESULT_V1,
			OutboxEventType.ALARM_PUSH_RESULT,
			event.userId().toString(),
			event
		);
	}
}
