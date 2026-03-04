package com.raisedeveloper.server.domain.exercise.application;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.event.AlarmEventPublisher;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionCreatedEvent;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionPushResultEvent;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.push.application.PushDeliveryStatus;
import com.raisedeveloper.server.domain.push.application.PushService;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.infra.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseSessionPushDispatchService {

	private final ExerciseSessionRepository exerciseSessionRepository;
	private final UserRepository userRepository;
	private final PushService pushService;
	private final AlarmEventPublisher alarmEventPublisher;

	@Transactional
	public void processSessionCreatedEvent(ExerciseSessionCreatedEvent event) {
		Optional<ExerciseSession> sessionOpt = exerciseSessionRepository.findByIdAndUserIdWithRoutine(
			event.sessionId(),
			event.userId()
		);
		if (sessionOpt.isEmpty()) {
			log.warn("Session not found for push event: eventId={}, sessionId={}, userId={}",
				event.eventId(), event.sessionId(), event.userId());
			publishPushResult(event, PushDeliveryStatus.FAILED_PERMANENT);
			return;
		}

		Optional<User> userOpt = userRepository.findById(event.userId());
		if (userOpt.isEmpty()) {
			log.warn("User not found for push event: eventId={}, sessionId={}, userId={}",
				event.eventId(), event.sessionId(), event.userId());
			publishPushResult(event, PushDeliveryStatus.FAILED_PERMANENT);
			return;
		}

		PushDeliveryStatus status = pushService.sendSessionPush(userOpt.get(), sessionOpt.get());
		publishPushResult(event, status);
	}

	private void publishPushResult(ExerciseSessionCreatedEvent sourceEvent, PushDeliveryStatus status) {
		alarmEventPublisher.publishPushResult(new ExerciseSessionPushResultEvent(
			UUID.randomUUID().toString(),
			sourceEvent.eventId(),
			sourceEvent.userId(),
			sourceEvent.sessionId(),
			status.name(),
			LocalDateTime.now()
		));
	}
}
