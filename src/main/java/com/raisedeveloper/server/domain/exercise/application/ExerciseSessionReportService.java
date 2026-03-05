package com.raisedeveloper.server.domain.exercise.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseResult;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSessionReport;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionReportDetailResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionReportListResponse;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseResultRepository;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionReportRepository;
import com.raisedeveloper.server.domain.exercise.mapper.ExerciseSessionReportMapper;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseSessionReportService {

	private final ExerciseSessionReportRepository exerciseSessionReportRepository;
	private final ExerciseResultRepository exerciseResultRepository;
	private final ExerciseSessionReportMapper exerciseSessionReportMapper;

	public ExerciseSessionReportListResponse getSessionReports(Long userId) {
		List<ExerciseSessionReport> reports = exerciseSessionReportRepository
			.findByUserIdOrderByCreatedAtDesc(userId);

		return exerciseSessionReportMapper.toListResponse(reports);
	}

	public ExerciseSessionReportDetailResponse getSessionReportDetail(Long userId, Long reportId) {
		ExerciseSessionReport report = exerciseSessionReportRepository
			.findByIdAndUserIdWithSession(reportId, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.EXERCISE_SESSION_REPORT_NOT_FOUND));

		List<ExerciseResult> exerciseResults = exerciseResultRepository
			.findByExerciseSessionIdWithDetails(report.getExerciseSession().getId());

		return exerciseSessionReportMapper.toDetailResponse(report, exerciseResults);
	}
}
