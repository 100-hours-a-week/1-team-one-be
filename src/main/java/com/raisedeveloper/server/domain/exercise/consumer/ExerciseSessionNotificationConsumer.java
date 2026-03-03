package com.raisedeveloper.server.domain.exercise.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raisedeveloper.server.domain.exercise.event.ExerciseKafkaTopics;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionRewardAppliedEvent;
import com.raisedeveloper.server.domain.notification.application.NotificationService;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.consumer.application.ConsumerIdempotencyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExerciseSessionNotificationConsumer {

	private final ObjectMapper objectMapper;
	private final ConsumerIdempotencyService consumerIdempotencyService;
	private final NotificationService notificationService;
	private final UserRepository userRepository;

	@KafkaListener(
		topics = ExerciseKafkaTopics.SESSION_REWARD_APPLIED,
		groupId = ExerciseConsumerConstants.NOTIFICATION_GROUP_ID
	)
	@Transactional
	public void consume(String message) throws Exception {
		ExerciseSessionRewardAppliedEvent event = objectMapper.readValue(message, ExerciseSessionRewardAppliedEvent.class);
		if (!consumerIdempotencyService.markProcessedIfFirst(
			ExerciseConsumerConstants.NOTIFICATION_CONSUMER_NAME,
			event.eventId()
		)) {
			log.info("Skip duplicate notification event: eventId={}", event.eventId());
			return;
		}

		User user = userRepository.getReferenceById(event.userId());
		if (event.completedCount() == 0) {
			notificationService.createStretchingFailed(user);
			return;
		}
		notificationService.createStretchingSuccess(user, event.earnedExp());
	}
}
