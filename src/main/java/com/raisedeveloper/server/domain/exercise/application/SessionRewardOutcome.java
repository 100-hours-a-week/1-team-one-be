package com.raisedeveloper.server.domain.exercise.application;

import com.raisedeveloper.server.domain.user.application.SessionRewardResult;

public record SessionRewardOutcome(
	int previousExp,
	int previousStatusScore,
	SessionRewardResult rewardResult
) {
}
