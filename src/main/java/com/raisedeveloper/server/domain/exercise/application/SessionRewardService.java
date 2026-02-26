package com.raisedeveloper.server.domain.exercise.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.user.application.SessionRewardResult;
import com.raisedeveloper.server.domain.user.application.UserCharacterService;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionRewardService {

	private final ExerciseSessionService exerciseSessionService;
	private final UserCharacterService userCharacterService;

	public SessionRewardPreparation prepare(Long userId) {
		boolean hadCompletedTodayBefore = exerciseSessionService.hasCompletedSessionToday(userId);
		UserCharacter character = userCharacterService.getByUserIdOrThrow(userId);

		return new SessionRewardPreparation(
			hadCompletedTodayBefore,
			character.getExp(),
			character.getStatusScore()
		);
	}

	@Transactional
	public SessionRewardOutcome apply(Long userId, long completedCount, SessionRewardPreparation preparation) {
		UserCharacter character = userCharacterService.getByUserIdOrThrow(userId);
		SessionRewardResult rewardResult = applyReward(
			character,
			completedCount,
			preparation.hadCompletedTodayBefore()
		);

		return new SessionRewardOutcome(
			preparation.previousExp(),
			preparation.previousStatusScore(),
			rewardResult
		);
	}

	private SessionRewardResult applyReward(
		UserCharacter character,
		long completedCount,
		boolean hadCompletedTodayBefore
	) {
		int earnedExp = calculateEarnedExp(completedCount);
		int earnedStatusScore = calculateEarnedStatusScore(completedCount);

		character.addExp(earnedExp);
		character.addStatusScore(earnedStatusScore);
		if (!hadCompletedTodayBefore) {
			character.incrementStreak();
		}

		return new SessionRewardResult(character, earnedExp, earnedStatusScore);
	}

	private int calculateEarnedExp(long completedCount) {
		return (int)completedCount * 10;
	}

	private int calculateEarnedStatusScore(long completedCount) {
		return (int)completedCount;
	}
}
