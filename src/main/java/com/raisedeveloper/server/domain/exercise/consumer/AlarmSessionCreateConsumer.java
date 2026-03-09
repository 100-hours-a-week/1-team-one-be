package com.raisedeveloper.server.domain.exercise.consumer;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raisedeveloper.server.domain.exercise.application.AlarmScheduleService;
import com.raisedeveloper.server.domain.exercise.application.ExerciseService;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.event.AlarmDueUserEvent;
import com.raisedeveloper.server.domain.exercise.event.AlarmEventPublisher;
import com.raisedeveloper.server.domain.exercise.event.AlarmSessionFailureReason;
import com.raisedeveloper.server.domain.exercise.event.AlarmSessionFailedEvent;
import com.raisedeveloper.server.domain.exercise.event.ExerciseKafkaTopics;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionCreatedEvent;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;
import com.raisedeveloper.server.domain.user.infra.UserAlarmSettingsRepository;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.consumer.application.ConsumerIdempotencyService;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmSessionCreateConsumer {

	private final ObjectMapper objectMapper;
	private final ConsumerIdempotencyService consumerIdempotencyService;
	private final UserRepository userRepository;
	private final UserAlarmSettingsRepository userAlarmSettingsRepository;
	private final ExerciseService exerciseService;
	private final AlarmEventPublisher alarmEventPublisher;
	private final AlarmScheduleService alarmScheduleService;

	@KafkaListener(
		topics = ExerciseKafkaTopics.ALARM_DUE_USER_V1,
		groupId = AlarmConsumerConstants.SESSION_CREATOR_GROUP_ID,
		concurrency = "${spring.app.kafka.alarm-due.concurrency:6}"
	)
	@Transactional
	public void consume(String message) throws Exception {
		AlarmDueUserEvent event = objectMapper.readValue(message, AlarmDueUserEvent.class);
		if (!consumerIdempotencyService.markProcessedIfFirst(
			AlarmConsumerConstants.SESSION_CREATOR_CONSUMER_NAME,
			event.eventId()
		)) {
			log.info("Skip duplicate alarm due-user event: eventId={}", event.eventId());
			return;
		}

		UserAlarmSettings settings = userAlarmSettingsRepository.findByUserId(event.userId()).orElse(null);
		if (settings == null) {
			publishFailure(event, AlarmSessionFailureReason.ALARM_SETTING_NOT_FOUND);
			alarmScheduleService.markClaimCompleted(event.userId());
			return;
		}

		try {
			User user = userRepository.findById(event.userId())
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
			ExerciseSession session = exerciseService.createScheduledSession(user, event.scheduledAt());
			alarmEventPublisher.publishSessionCreated(new ExerciseSessionCreatedEvent(
				UUID.randomUUID().toString(),
				event.eventId(),
				event.userId(),
				session.getId(),
				event.scheduledAt(),
				LocalDateTime.now()
			));
			alarmScheduleService.advanceAfterDue(settings, event.scheduledAt());
			alarmScheduleService.markClaimCompleted(event.userId());
		} catch (CustomException e) {
			if (e.getErrorCode() == ErrorCode.ROUTINE_NOT_FOUND) {
				publishFailure(event, AlarmSessionFailureReason.ROUTINE_NOT_FOUND);
				alarmScheduleService.advanceAfterDue(settings, event.scheduledAt());
				alarmScheduleService.markClaimCompleted(event.userId());
				return;
			}
			if (e.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
				publishFailure(event, AlarmSessionFailureReason.USER_NOT_FOUND);
				alarmScheduleService.markClaimCompleted(event.userId());
				return;
			}
			throw e;
		}
	}

	private void publishFailure(AlarmDueUserEvent event, AlarmSessionFailureReason reason) {
		alarmEventPublisher.publishSessionFailed(new AlarmSessionFailedEvent(
			UUID.randomUUID().toString(),
			event.eventId(),
			event.userId(),
			event.scheduledAt(),
			reason.name(),
			LocalDateTime.now()
		));
	}
}
