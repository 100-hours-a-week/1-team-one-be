package com.raisedeveloper.server.domain.exercise.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

	private static final int MAX_IN_FLIGHT = 100;

	private final ObjectMapper objectMapper;
	private final ConsumerIdempotencyService consumerIdempotencyService;
	private final ExerciseSessionPushDispatchService exerciseSessionPushDispatchService;
	private final MeterRegistry meterRegistry;

	@KafkaListener(
		topics = ExerciseKafkaTopics.ALARM_SESSION_CREATED_V1,
		groupId = AlarmConsumerConstants.PUSH_GROUP_ID,
		containerFactory = "pushBatchKafkaListenerContainerFactory",
		concurrency = "${spring.app.kafka.alarm-push.concurrency:6}"
	)
	public void consume(List<String> messages) throws Exception {
		List<CompletableFuture<Void>> inFlight = new ArrayList<>(MAX_IN_FLIGHT);
		for (String message : messages) {
			ExerciseSessionCreatedEvent event = objectMapper.readValue(message, ExerciseSessionCreatedEvent.class);
			long startedAt = System.currentTimeMillis();
			String eventId = event.eventId();
			Long userId = event.userId();
			Long sessionId = event.sessionId();

			if (consumerIdempotencyService.markProcessedIfFirst(
				AlarmConsumerConstants.PUSH_CONSUMER_NAME,
				event.eventId()
			)) {
				CompletableFuture<Void> future = exerciseSessionPushDispatchService.processSessionCreatedEventAsync(event)
					.whenComplete((unused, throwable) -> {
						String status = throwable == null ? "success" : "failure";
						if (throwable == null) {
							log.info("PushConsumer 처리 완료 - eventId: {}, userId: {}, sessionId: {}, elapsedMs: {}",
								eventId, userId, sessionId, System.currentTimeMillis() - startedAt);
						} else {
							log.error("PushConsumer 처리 실패 - eventId: {}, userId: {}, sessionId: {}, elapsedMs: {}",
								eventId, userId, sessionId, System.currentTimeMillis() - startedAt, throwable);
						}
						recordMetric(status, System.currentTimeMillis() - startedAt);
					});
				inFlight.add(future);
				if (inFlight.size() >= MAX_IN_FLIGHT) {
					awaitAll(inFlight);
				}
				continue;
			}

			log.info("PushConsumer 중복 스킵 - eventId: {}, userId: {}, sessionId: {}, elapsedMs: {}",
				eventId, userId, sessionId, System.currentTimeMillis() - startedAt);
			recordMetric("duplicate", System.currentTimeMillis() - startedAt);
		}

		awaitAll(inFlight);
	}

	private void awaitAll(List<CompletableFuture<Void>> inFlight) {
		if (inFlight.isEmpty()) {
			return;
		}
		CompletableFuture<Void> all = CompletableFuture.allOf(inFlight.toArray(CompletableFuture[]::new));
		try {
			all.join();
		} catch (CompletionException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw new RuntimeException(cause);
		} finally {
			inFlight.clear();
		}
	}

	private void recordMetric(String status, long elapsedMs) {
		meterRegistry.timer(
			"kafka.consumer.push.process.duration",
			"topic", ExerciseKafkaTopics.ALARM_SESSION_CREATED_V1,
			"group", AlarmConsumerConstants.PUSH_GROUP_ID,
			"status", status
		).record(elapsedMs, TimeUnit.MILLISECONDS);
	}
}
