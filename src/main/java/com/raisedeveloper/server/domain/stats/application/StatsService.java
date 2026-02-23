package com.raisedeveloper.server.domain.stats.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.stats.dto.GrassStatsProjection;
import com.raisedeveloper.server.domain.stats.dto.GrassStatsResponse;
import com.raisedeveloper.server.domain.stats.enums.ViewType;
import com.raisedeveloper.server.domain.stats.mapper.StatsMapper;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

	private final ExerciseSessionRepository exerciseSessionRepository;
	private final StatsMapper statsMapper;

	public GrassStatsResponse getGrassStats(Long userId, ViewType viewType, String month) {
		LocalDateTime startDate;
		LocalDateTime endDate;

		if (viewType == ViewType.WEEKLY) {
			LocalDate today = LocalDate.now();
			startDate = today.minusDays(6).atStartOfDay();
			endDate = today.plusDays(1).atStartOfDay();
		} else {
			if (month == null || month.isBlank()) {
				throw new CustomException(
					ErrorCode.VALIDATION_FAILED,
					List.of(ErrorDetail.field("month", "month is required for monthly view"))
				);
			}

			try {
				YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
				startDate = yearMonth.atDay(1).atStartOfDay();
				endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
			} catch (DateTimeParseException e) {
				throw new CustomException(
					ErrorCode.VALIDATION_FAILED,
					List.of(ErrorDetail.field("month", "month format must be yyyy-MM"))
				);
			}
		}

		List<GrassStatsProjection> projections = exerciseSessionRepository.findGrassStatsByDateRange(
			userId,
			startDate,
			endDate
		);

		GrassStatsResponse response = statsMapper.toGrassStatsResponse(projections);

		log.info("Grass stats retrieved: userId={}, viewType={}, dataCount={}", userId, viewType,
			response.grass().size());

		return response;
	}
}
