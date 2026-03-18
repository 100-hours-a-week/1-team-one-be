package com.raisedeveloper.server.domain.exercise.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raisedeveloper.server.domain.exercise.event.ExerciseKafkaTopics;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionRewardAppliedEvent;
import com.raisedeveloper.server.domain.quest.application.QuestProgressService;
import com.raisedeveloper.server.global.consumer.application.ConsumerIdempotencyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestConsumer {

	private final ObjectMapper objectMapper;
	private final ConsumerIdempotencyService consumerIdempotencyService;
	private final QuestProgressService questProgressService;

	@KafkaListener(
		topics = ExerciseKafkaTopics.SESSION_REWARD_APPLIED,
		groupId = ExerciseConsumerConstants.QUEST_GROUP_ID
	)
	@Transactional
	public void consume(String message) throws Exception {
		ExerciseSessionRewardAppliedEvent event = objectMapper.readValue(message,
			ExerciseSessionRewardAppliedEvent.class);
		if (!consumerIdempotencyService.markProcessedIfFirst(
			ExerciseConsumerConstants.QUEST_CONSUMER_NAME,
			event.eventId()
		)) {
			log.info("Skip duplicate quest event: eventId={}", event.eventId());
			return;
		}

		if (event.completedCount() == 0 || !event.firstCompletionToday()) {
			return;
		}

		questProgressService.updateStretchingStreakQuests(event.userId(), event.occurredAt());
	}
}
