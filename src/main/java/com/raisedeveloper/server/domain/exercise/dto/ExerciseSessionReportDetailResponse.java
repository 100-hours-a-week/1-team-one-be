package com.raisedeveloper.server.domain.exercise.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ExerciseSessionReportDetailResponse(
	Long sessionReportId,
	LocalDateTime createdAt,
	Boolean isRoutineCompleted,
	List<SessionReportExerciseDto> exercises,
	SessionReportRewardsDto rewards
) {
}
