package com.raisedeveloper.server.domain.stats.dto;

import java.util.List;

public record GrassStatsResponse(
	List<GrassDataDto> grass
) {
}
