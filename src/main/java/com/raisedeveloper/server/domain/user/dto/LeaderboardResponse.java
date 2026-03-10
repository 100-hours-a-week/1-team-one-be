package com.raisedeveloper.server.domain.user.dto;

import java.util.List;

import com.raisedeveloper.server.global.pagination.PagingResponse;

public record LeaderboardResponse(
	List<LeaderboardRankItem> podium,
	List<LeaderboardRankItem> ranks,
	LeaderboardRankItem myRank,
	PagingResponse paging
) {
}
