package com.raisedeveloper.server.domain.exercise.application;

public record SessionRewardPreparation(
	boolean hadCompletedTodayBefore,
	int previousExp,
	int previousStatusScore
) {
}
