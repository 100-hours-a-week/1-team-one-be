package com.raisedeveloper.server.domain.exercise.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSessionReport;
import com.raisedeveloper.server.domain.exercise.event.ExerciseKafkaTopics;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionRewardAppliedEvent;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionReportRepository;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.consumer.application.ConsumerIdempotencyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExerciseSessionReportConsumer {

	private final ObjectMapper objectMapper;
	private final ConsumerIdempotencyService consumerIdempotencyService;
	private final ExerciseSessionReportRepository exerciseSessionReportRepository;
	private final ExerciseSessionRepository exerciseSessionRepository;
	private final UserRepository userRepository;

	@KafkaListener(
		topics = ExerciseKafkaTopics.SESSION_REWARD_APPLIED,
		groupId = ExerciseConsumerConstants.REPORT_GROUP_ID
	)
	@Transactional
	public void consume(String message) throws Exception {
		ExerciseSessionRewardAppliedEvent event = objectMapper.readValue(message, ExerciseSessionRewardAppliedEvent.class);
		if (!consumerIdempotencyService.markProcessedIfFirst(
			ExerciseConsumerConstants.REPORT_CONSUMER_NAME,
			event.eventId()
		)) {
			log.info("Skip duplicate report event: eventId={}", event.eventId());
			return;
		}

		if (exerciseSessionReportRepository.existsByExerciseSessionId(event.sessionId())) {
			log.info("Report already exists for sessionId={}, skip", event.sessionId());
			return;
		}

		ExerciseSession session = exerciseSessionRepository.getReferenceById(event.sessionId());
		User user = userRepository.getReferenceById(event.userId());
		exerciseSessionReportRepository.save(new ExerciseSessionReport(
			session,
			user,
			event.level(),
			event.previousExp(),
			event.earnedExp(),
			event.streak(),
			event.previousStatusScore(),
			event.earnedStatusScore()
		));
	}
}
