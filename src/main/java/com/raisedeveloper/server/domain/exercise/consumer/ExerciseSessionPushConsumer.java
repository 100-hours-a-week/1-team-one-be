package com.raisedeveloper.server.domain.exercise.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raisedeveloper.server.domain.exercise.application.ExerciseSessionPushDispatchService;
import com.raisedeveloper.server.domain.exercise.event.ExerciseKafkaTopics;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionCreatedEvent;
import com.raisedeveloper.server.global.consumer.application.ConsumerIdempotencyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExerciseSessionPushConsumer {

	private final ObjectMapper objectMapper;
	private final ConsumerIdempotencyService consumerIdempotencyService;
	private final ExerciseSessionPushDispatchService exerciseSessionPushDispatchService;

	@KafkaListener(
		topics = ExerciseKafkaTopics.ALARM_SESSION_CREATED_V1,
		groupId = AlarmConsumerConstants.PUSH_GROUP_ID
	)
	public void consume(String message) throws Exception {
		ExerciseSessionCreatedEvent event = objectMapper.readValue(message, ExerciseSessionCreatedEvent.class);

		if (consumerIdempotencyService.markProcessedIfFirst(
			AlarmConsumerConstants.PUSH_CONSUMER_NAME,
			event.eventId()
		)) {
			exerciseSessionPushDispatchService.processSessionCreatedEvent(event);
		}
	}
}
