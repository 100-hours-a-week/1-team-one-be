package com.raisedeveloper.server.domain.stats.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.stats.application.StatsService;
import com.raisedeveloper.server.domain.stats.dto.GrassStatsResponse;
import com.raisedeveloper.server.domain.stats.dto.StatsSummaryResponse;
import com.raisedeveloper.server.domain.stats.enums.ViewType;
import com.raisedeveloper.server.global.response.ApiResponse;
import com.raisedeveloper.server.global.security.currentuser.CurrentUser;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/me/stats")
@RequiredArgsConstructor
public class StatsController {

	private final StatsService statsService;

	@GetMapping("/summary")
	public ApiResponse<StatsSummaryResponse> getSummaryStats(@CurrentUser Long userId) {
		StatsSummaryResponse response = statsService.getSummaryStats(userId);
		return ApiResponse.of("GET_STATS_SUMMARY_SUCCESS", response);
	}

	@GetMapping("/grass")
	public ApiResponse<GrassStatsResponse> getGrassStats(
		@CurrentUser Long userId,
		@RequestParam("view") ViewType viewType,
		@RequestParam(value = "month", required = false) String month
	) {
		GrassStatsResponse response = statsService.getGrassStats(userId, viewType, month);
		return ApiResponse.of("GET_STATS_GRASS_SUCCESS", response);
	}
}
