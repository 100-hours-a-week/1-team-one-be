package com.raisedeveloper.server.domain.stats.dto;

public record StatsSummaryResponse(
	int streak,
	long todaySuccess,
	long weeklySuccess,
	long lastWeekSuccess
) {
}
