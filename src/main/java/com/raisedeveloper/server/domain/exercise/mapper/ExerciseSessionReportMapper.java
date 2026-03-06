package com.raisedeveloper.server.domain.exercise.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseResult;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSessionReport;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionReportDetailResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionReportListResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionReportSummaryDto;
import com.raisedeveloper.server.domain.exercise.dto.SessionReportExerciseDto;
import com.raisedeveloper.server.domain.exercise.dto.SessionReportRewardsDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExerciseSessionReportMapper {

	public ExerciseSessionReportListResponse toListResponse(List<ExerciseSessionReport> reports) {
		List<ExerciseSessionReportSummaryDto> summaryList = reports.stream()
			.map(this::toSummaryDto)
			.toList();

		return new ExerciseSessionReportListResponse(summaryList);
	}

	private ExerciseSessionReportSummaryDto toSummaryDto(ExerciseSessionReport report) {
		return new ExerciseSessionReportSummaryDto(
			report.getId(),
			report.getCreatedAt()
		);
	}

	public ExerciseSessionReportDetailResponse toDetailResponse(
		ExerciseSessionReport report,
		List<ExerciseResult> exerciseResults
	) {
		List<SessionReportExerciseDto> exercises = exerciseResults.stream()
			.map(this::toExerciseDto)
			.toList();

		SessionReportRewardsDto rewards = toRewardsDto(report);

		return new ExerciseSessionReportDetailResponse(
			report.getId(),
			report.getCreatedAt(),
			report.getExerciseSession().getIsRoutineCompleted(),
			exercises,
			rewards
		);
	}

	private SessionReportExerciseDto toExerciseDto(ExerciseResult result) {
		return new SessionReportExerciseDto(
			result.getRoutineStep().getExercise().getId(),
			result.getRoutineStep().getExercise().getName(),
			result.getRoutineStep().getExercise().getType(),
			result.getRoutineStep().getStepOrder(),
			result.getStatus(),
			result.getAccuracy() == 0 ? null : result.getAccuracy()
		);
	}

	private SessionReportRewardsDto toRewardsDto(ExerciseSessionReport report) {
		return new SessionReportRewardsDto(
			report.getLevel(),
			report.getPreviousExp(),
			report.getEarnedExp(),
			report.getStreak(),
			report.getPreviousStatusScore(),
			report.getEarnedStatusScore()
		);
	}
}
