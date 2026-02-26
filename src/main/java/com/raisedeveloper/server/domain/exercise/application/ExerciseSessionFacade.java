package com.raisedeveloper.server.domain.exercise.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSessionReport;
import com.raisedeveloper.server.domain.exercise.dto.CharacterDto;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionCompleteResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionReportCreateResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionUpdateRequest;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionReportRepository;
import com.raisedeveloper.server.domain.exercise.mapper.ExerciseSessionMapper;
import com.raisedeveloper.server.domain.notification.application.NotificationService;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseSessionFacade {

	private final ExerciseSessionService exerciseSessionService;
	private final SessionRewardService sessionRewardService;
	private final NotificationService notificationService;
	private final ExerciseSessionReportRepository exerciseSessionReportRepository;
	private final ExerciseSessionMapper exerciseSessionMapper;

	@Transactional
	public ExerciseSessionCompleteResponse completeExerciseSession(
		Long userId,
		Long sessionId,
		ExerciseSessionUpdateRequest request
	) {
		ExerciseSession session = exerciseSessionService.getSessionForUpdate(userId, sessionId);
		long completedCount = updateSessionData(session, request);
		AppliedSessionReward reward = sessionRewardService.applyForCompletedSession(userId, completedCount);

		int earnedExp = reward.earnedExp();
		int earnedStatusScore = reward.earnedStatusScore();
		CharacterDto characterDto = exerciseSessionMapper.toCharacterDto(reward.character());

		log.info("Exercise session completed: sessionId={}, userId={}, earnedExp={}, earnedStatusScore={}",
			sessionId, userId, earnedExp, earnedStatusScore);

		if (completedCount == 0) {
			notificationService.createStretchingFailed(session.getUser());
		} else {
			notificationService.createStretchingSuccess(session.getUser(), earnedExp);
		}

		return exerciseSessionMapper.toCompleteResponse(
			sessionId,
			earnedExp,
			earnedStatusScore,
			characterDto,
			List.of() // TODO: Quest 기능 구현 시 실제 퀘스트 진행도 반환
		);
	}

	@Transactional
	public ExerciseSessionReportCreateResponse completeExerciseSessionV2(
		Long userId,
		Long sessionId,
		ExerciseSessionUpdateRequest request
	) {
		ExerciseSession session = exerciseSessionService.getSessionForUpdate(userId, sessionId);
		long completedCount = updateSessionData(session, request);
		AppliedSessionReward reward = sessionRewardService.applyForCompletedSession(userId, completedCount);

		UserCharacter character = reward.character();
		ExerciseSessionReport exerciseSessionReport = exerciseSessionReportRepository.save(
			new ExerciseSessionReport(
				session,
				session.getUser(),
				character.getLevel(),
				reward.previousExp(),
				reward.earnedExp(),
				character.getStreak(),
				reward.previousStatusScore(),
				reward.earnedStatusScore()
			)
		);

		log.info("Exercise session v2 completed and report created: sessionId={}, userId={}, reportId={}",
			sessionId, userId, exerciseSessionReport.getId());

		if (completedCount == 0) {
			notificationService.createStretchingFailed(session.getUser());
		} else {
			notificationService.createStretchingSuccess(
				session.getUser(), reward.earnedExp()
			);
		}

		return new ExerciseSessionReportCreateResponse(
			session.getId(),
			exerciseSessionReport.getId(),
			session.getRoutine().getId(),
			session.getStartAt(),
			session.getEndAt(),
			session.getIsRoutineCompleted()
		);
	}

	private long updateSessionData(
		ExerciseSession session,
		ExerciseSessionUpdateRequest request
	) {
		return exerciseSessionService.updateSessionAndResults(session, request);
	}
}
