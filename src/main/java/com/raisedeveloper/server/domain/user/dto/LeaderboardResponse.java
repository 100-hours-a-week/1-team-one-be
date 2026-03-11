package com.raisedeveloper.server.domain.user.dto;

import java.util.List;

import com.raisedeveloper.server.global.pagination.BiDirectionPagingResponse;

public record LeaderboardResponse(
	List<LeaderboardRankItem> podium,
	List<LeaderboardRankItem> ranks,
	LeaderboardRankItem myRank,
	BiDirectionPagingResponse paging
) {
}
