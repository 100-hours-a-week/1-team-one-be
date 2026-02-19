package com.raisedeveloper.server.domain.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.infra.UserCharacterRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCharacterService {

	private final UserCharacterRepository userCharacterRepository;

	@Transactional
	public SessionRewardResult applySessionReward(
		Long userId,
		long completedCount,
		boolean hasCompletedToday
	) {
		UserCharacter character = userCharacterRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHARACTER_NOT_SET));

		int earnedExp = calculateEarnedExp(completedCount);
		int earnedStatusScore = calculateEarnedStatusScore(completedCount);

		character.addExp(earnedExp);
		character.addStatusScore(earnedStatusScore);
		if (!hasCompletedToday) {
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
