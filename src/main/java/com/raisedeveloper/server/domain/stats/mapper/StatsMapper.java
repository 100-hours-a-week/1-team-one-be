package com.raisedeveloper.server.domain.stats.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.stats.dto.GrassDataDto;
import com.raisedeveloper.server.domain.stats.dto.GrassStatsProjection;
import com.raisedeveloper.server.domain.stats.dto.GrassStatsResponse;

@Component
public class StatsMapper {

	public List<GrassDataDto> toGrassData(List<GrassStatsProjection> projections) {
		return projections.stream()
			.map(p -> new GrassDataDto(
				p.getDate(),
				p.getTargetCount() != null ? p.getTargetCount().intValue() : 0,
				p.getSuccessCount() != null ? p.getSuccessCount().intValue() : 0
			))
			.toList();
	}

	public GrassStatsResponse toGrassStatsResponse(List<GrassStatsProjection> projections) {
		return new GrassStatsResponse(toGrassData(projections));
	}
}
