package com.raisedeveloper.server.domain.user.application;

import com.raisedeveloper.server.domain.user.domain.UserCharacter;

public record SessionRewardResult(
	UserCharacter character,
	int earnedExp,
	int earnedStatusScore
) {
}
