package com.raisedeveloper.server.domain.exercise.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseResult;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseResultRepository;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.notification.application.NotificationService;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.infra.UserCharacterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExerciseSessionFailScheduler {

	private static final long STALE_MINUTES = 15;

	private final ExerciseSessionRepository exerciseSessionRepository;
	private final ExerciseResultRepository exerciseResultRepository;
	private final UserCharacterRepository userCharacterRepository;
	private final NotificationService notificationService;

	@Scheduled(cron = "0 */5 * * * *")
	@SchedulerLock(name = "ExerciseSessionFailScheduler.failStaleSessions", lockAtMostFor = "PT10M")
	@Transactional
	public void failStaleSessions() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime cutoff = now.minusMinutes(STALE_MINUTES);

		List<ExerciseSession> staleSessions = exerciseSessionRepository.findStaleUnupdatedSessions(cutoff);
		if (staleSessions.isEmpty()) {
			return;
		}

		List<Long> sessionIds = staleSessions.stream()
			.map(ExerciseSession::getId)
			.toList();

		List<ExerciseResult> results = exerciseResultRepository.findByExerciseSessionIds(sessionIds);
		Map<Long, Long> failedResultCounts = results.stream()
			.collect(Collectors.groupingBy(
				result -> result.getExerciseSession().getId(),
				Collectors.counting()
			));

		results.forEach(result -> result.stepResultsFailed(now));

		for (ExerciseSession session : staleSessions) {
			session.sessionFailed(now);
			int failedCount = Math.toIntExact(
				failedResultCounts.getOrDefault(session.getId(), 0L)
			);

			if (failedCount <= 0) {
				continue;
			}

			UserCharacter character = userCharacterRepository.findByUserId(session.getUser().getId())
				.orElse(null);
			if (character == null) {
				log.warn("캐릭터가 없어 세션 실패 패널티 스킵 - userId: {}, sessionId: {}",
					session.getUser().getId(), session.getId());
				continue;
			}

			character.subtractStatusScore(failedCount);
			notificationService.createStretchingFailed(session.getUser());
		}
		log.info("세션 자동 실패 처리 완료: {} 건", staleSessions.size());
	}
}
