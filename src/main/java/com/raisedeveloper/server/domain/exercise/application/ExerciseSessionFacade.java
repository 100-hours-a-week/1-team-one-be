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
import com.raisedeveloper.server.domain.user.application.SessionRewardResult;
import com.raisedeveloper.server.domain.user.application.UserCharacterService;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.infra.UserCharacterRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseSessionFacade {

	private final ExerciseSessionService exerciseSessionService;
	private final UserCharacterService userCharacterService;
	private final NotificationService notificationService;
	private final ExerciseSessionReportRepository exerciseSessionReportRepository;
	private final UserCharacterRepository userCharacterRepository;
	private final ExerciseSessionMapper exerciseSessionMapper;

	@Transactional
	public ExerciseSessionCompleteResponse completeExerciseSession(
		Long userId,
		Long sessionId,
		ExerciseSessionUpdateRequest request
	) {
		ExerciseSession session = exerciseSessionService.getSessionForUpdate(userId, sessionId);
		long completedCount = updateSessionData(session, request);

		SessionRewardResult rewardResult = userCharacterService.applySessionReward(
			userId,
			completedCount,
			exerciseSessionService.hasCompletedSessionToday(userId)
		);

		int earnedExp = rewardResult.earnedExp();
		int earnedStatusScore = rewardResult.earnedStatusScore();
		CharacterDto characterDto = exerciseSessionMapper.toCharacterDto(rewardResult.character());

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

		UserCharacter beforeReward = userCharacterRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHARACTER_NOT_SET));
		int previousExp = beforeReward.getExp();
		int previousStatusScore = beforeReward.getStatusScore();

		SessionRewardResult rewardResult = userCharacterService.applySessionReward(
			userId,
			completedCount,
			exerciseSessionService.hasCompletedSessionToday(userId)
		);

		UserCharacter character = rewardResult.character();
		ExerciseSessionReport exerciseSessionReport = exerciseSessionReportRepository.save(
			new ExerciseSessionReport(
				session,
				session.getUser(),
				character.getLevel(),
				previousExp,
				rewardResult.earnedExp(),
				character.getStreak(),
				previousStatusScore,
				rewardResult.earnedStatusScore()
			)
		);

		log.info("Exercise session v2 completed and report created: sessionId={}, userId={}, reportId={}",
			sessionId, userId, exerciseSessionReport.getId());

		if (completedCount == 0) {
			notificationService.createStretchingFailed(session.getUser());
		} else {
			notificationService.createStretchingSuccess(
				session.getUser(),
				rewardResult.earnedExp()
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
