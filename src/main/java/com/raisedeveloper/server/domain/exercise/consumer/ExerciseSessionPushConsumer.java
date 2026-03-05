package com.raisedeveloper.server.domain.exercise.consumer;

import java.util.concurrent.TimeUnit;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raisedeveloper.server.domain.exercise.application.ExerciseSessionPushDispatchService;
import com.raisedeveloper.server.domain.exercise.event.ExerciseKafkaTopics;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionCreatedEvent;
import com.raisedeveloper.server.global.consumer.application.ConsumerIdempotencyService;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExerciseSessionPushConsumer {

	private final ObjectMapper objectMapper;
	private final ConsumerIdempotencyService consumerIdempotencyService;
	private final ExerciseSessionPushDispatchService exerciseSessionPushDispatchService;
	private final MeterRegistry meterRegistry;

	@KafkaListener(
		topics = ExerciseKafkaTopics.ALARM_SESSION_CREATED_V1,
		groupId = AlarmConsumerConstants.PUSH_GROUP_ID,
		concurrency = "${spring.app.kafka.alarm-push.concurrency:6}"
	)
	public void consume(String message) throws Exception {
		long startedAt = System.currentTimeMillis();
		String eventId = "UNKNOWN";
		Long userId = null;
		Long sessionId = null;
		boolean duplicate = false;
		String status = "success";

		try {
			ExerciseSessionCreatedEvent event = objectMapper.readValue(message, ExerciseSessionCreatedEvent.class);
			eventId = event.eventId();
			userId = event.userId();
			sessionId = event.sessionId();

			if (consumerIdempotencyService.markProcessedIfFirst(
				AlarmConsumerConstants.PUSH_CONSUMER_NAME,
				event.eventId()
			)) {
				exerciseSessionPushDispatchService.processSessionCreatedEvent(event);
				log.info("PushConsumer 처리 완료 - eventId: {}, userId: {}, sessionId: {}, elapsedMs: {}",
					eventId, userId, sessionId, System.currentTimeMillis() - startedAt);
				return;
			}

			duplicate = true;
			status = "duplicate";
			log.info("PushConsumer 중복 스킵 - eventId: {}, userId: {}, sessionId: {}, elapsedMs: {}",
				eventId, userId, sessionId, System.currentTimeMillis() - startedAt);
		} catch (Exception e) {
			status = "failure";
			log.error("PushConsumer 처리 실패 - eventId: {}, userId: {}, sessionId: {}, duplicate: {}, elapsedMs: {}",
				eventId, userId, sessionId, duplicate, System.currentTimeMillis() - startedAt, e);
			throw e;
		} finally {
			long elapsedMs = System.currentTimeMillis() - startedAt;
			meterRegistry.timer(
				"kafka.consumer.push.process.duration",
				"topic", ExerciseKafkaTopics.ALARM_SESSION_CREATED_V1,
				"group", AlarmConsumerConstants.PUSH_GROUP_ID,
				"status", status
			).record(elapsedMs, TimeUnit.MILLISECONDS);
		}
	}
}
