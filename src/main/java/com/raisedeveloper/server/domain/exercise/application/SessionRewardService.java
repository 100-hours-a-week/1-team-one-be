package com.raisedeveloper.server.domain.exercise.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.user.application.UserCharacterService;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionRewardService {

	private final UserCharacterService userCharacterService;

	@Transactional
	public AppliedSessionReward applyForCompletedSession(Long userId, long completedCount,
		boolean firstCompletionToday) {
		UserCharacter character = userCharacterService.getByUserIdOrThrow(userId);
		int previousExp = character.getExp();
		int previousStatusScore = character.getStatusScore();

		int earnedExp = calculateEarnedExp(completedCount);
		int earnedStatusScore = calculateEarnedStatusScore(completedCount);

		character.addExp(earnedExp);
		character.addStatusScore(earnedStatusScore);
		if (firstCompletionToday) {
			character.incrementStreak();
		}

		return new AppliedSessionReward(
			character,
			previousExp,
			previousStatusScore,
			earnedExp,
			earnedStatusScore
		);
	}

	private int calculateEarnedExp(long completedCount) {
		return (int)completedCount * 10;
	}

	private int calculateEarnedStatusScore(long completedCount) {
		return (int)completedCount;
	}
}
