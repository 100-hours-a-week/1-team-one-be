package com.raisedeveloper.server.domain.exercise.application;

import com.raisedeveloper.server.domain.user.domain.UserCharacter;

public record AppliedSessionReward(
	UserCharacter character,
	int previousExp,
	int previousStatusScore,
	int earnedExp,
	int earnedStatusScore
) {
}
