package com.raisedeveloper.server.domain.exercise.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.event.AlarmEventPublisher;
import com.raisedeveloper.server.domain.exercise.event.ExerciseSessionCreatedEvent;
import com.raisedeveloper.server.domain.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlarmSessionDispatchService {

	private final ExerciseService exerciseService;
	private final AlarmEventPublisher alarmEventPublisher;

	@Transactional
	public ExerciseSession createSessionAndPublish(User user, LocalDateTime scheduledAt) {
		ExerciseSession session = exerciseService.createSession(user);

		alarmEventPublisher.publishSessionCreated(new ExerciseSessionCreatedEvent(
			UUID.randomUUID().toString(),
			null,
			session.getUser().getId(),
			session.getId(),
			scheduledAt,
			session.getCreatedAt()
		));
		return session;
	}
}
