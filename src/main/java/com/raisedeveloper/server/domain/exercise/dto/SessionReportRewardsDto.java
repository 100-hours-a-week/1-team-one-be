package com.raisedeveloper.server.domain.exercise.dto;

public record SessionReportRewardsDto(
	short level,
	int previousExp,
	int earnedExp,
	int streak,
	int previousStatusScore,
	int earnedStatusScore
) {
}
