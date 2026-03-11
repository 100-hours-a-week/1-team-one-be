package com.raisedeveloper.server.domain.user.dto;

public record LeaderboardRankItem(
	long rank,
	Long userId,
	String nickname,
	String profileImageUrl,
	short level,
	int exp,
	int statusScore,
	int streak
) {
}
