package com.raisedeveloper.server.domain.exercise.consumer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raisedeveloper.server.domain.exercise.application.AppliedSessionReward;
import com.raisedeveloper.server.domain.exercise.application.SessionRewardService;
import com.raisedeveloper.server.domain.exercise.event.ExerciseKafkaTopics;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionCompletedEvent;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionEventPublisher;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionRewardAppliedEvent;
import com.raisedeveloper.server.global.consumer.application.ConsumerIdempotencyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExerciseSessionRewardConsumer {

	private final ObjectMapper objectMapper;
	private final ConsumerIdempotencyService consumerIdempotencyService;
	private final SessionRewardService sessionRewardService;
	private final ExerciseSessionEventPublisher exerciseSessionEventPublisher;

	@KafkaListener(
		topics = ExerciseKafkaTopics.SESSION_COMPLETED,
		groupId = ExerciseConsumerConstants.REWARD_GROUP_ID
	)
	@Transactional
	public void consume(String message) throws Exception {
		ExerciseSessionCompletedEvent event = objectMapper.readValue(message, ExerciseSessionCompletedEvent.class);
		if (!consumerIdempotencyService.markProcessedIfFirst(
			ExerciseConsumerConstants.REWARD_CONSUMER_NAME,
			event.eventId()
		)) {
			log.info("Skip duplicate reward event: eventId={}", event.eventId());
			return;
		}

		AppliedSessionReward reward = sessionRewardService.applyForCompletedSession(
			event.userId(),
			event.completedCount()
		);
		exerciseSessionEventPublisher.publishRewardApplied(new ExerciseSessionRewardAppliedEvent(
			UUID.randomUUID().toString(),
			event.eventId(),
			LocalDateTime.now(),
			event.sessionId(),
			event.userId(),
			event.routineId(),
			event.startAt(),
			event.endAt(),
			event.routineCompleted(),
			event.completedCount(),
			reward.character().getLevel(),
			reward.previousExp(),
			reward.earnedExp(),
			reward.character().getStreak(),
			reward.previousStatusScore(),
			reward.earnedStatusScore()
		));
	}
}
