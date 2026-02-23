package com.raisedeveloper.server.domain.exercise.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.dto.CharacterDto;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionCompleteResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionUpdateRequest;
import com.raisedeveloper.server.domain.exercise.mapper.ExerciseSessionMapper;
import com.raisedeveloper.server.domain.notification.application.NotificationService;
import com.raisedeveloper.server.domain.user.application.SessionRewardResult;
import com.raisedeveloper.server.domain.user.application.UserCharacterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseSessionFacade {

	private final ExerciseSessionService exerciseSessionService;
	private final UserCharacterService userCharacterService;
	private final NotificationService notificationService;
	private final ExerciseSessionMapper exerciseSessionMapper;

	@Transactional
	public ExerciseSessionCompleteResponse completeExerciseSession(
		Long userId,
		Long sessionId,
		ExerciseSessionUpdateRequest request
	) {
		ExerciseSession session = exerciseSessionService.getSessionForUpdate(userId, sessionId);
		long completedCount = exerciseSessionService.updateSessionAndResults(session, request);

		boolean hasCompletedToday = exerciseSessionService.hasCompletedSessionToday(userId);

		SessionRewardResult rewardResult = userCharacterService.applySessionReward(
			userId,
			completedCount,
			hasCompletedToday
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
}
