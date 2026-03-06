package com.raisedeveloper.server.domain.exercise.application;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.event.AlarmEventPublisher;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionCreatedEvent;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionPushResultEvent;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.push.application.PushDeliveryStatus;
import com.raisedeveloper.server.domain.push.application.PushService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseSessionPushDispatchService {

	private final ExerciseSessionRepository exerciseSessionRepository;
	private final PushService pushService;
	private final AlarmEventPublisher alarmEventPublisher;

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

		ExerciseSession session = sessionOpt.get();
		PushDeliveryStatus status = pushService.sendSessionPush(session.getUser(), session);
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
