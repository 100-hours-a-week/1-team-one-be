package com.raisedeveloper.server.domain.user.application;

public record LeaderboardCursor(
	long snapshotVersion,
	long lastRank
) {
}
