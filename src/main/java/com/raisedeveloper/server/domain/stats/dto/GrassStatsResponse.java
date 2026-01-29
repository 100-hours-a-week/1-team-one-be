package com.raisedeveloper.server.domain.stats.dto;

import java.util.List;

public record GrassStatsResponse(
	List<GrassDataDto> grass
) {
	public static GrassStatsResponse from(List<GrassDataDto> grassData) {
		return new GrassStatsResponse(grassData);
	}
}
