package com.raisedeveloper.server.domain.exercise.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseResult;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseResultRequest;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionUpdateRequest;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionValidListResponse;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseResultRepository;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.exercise.mapper.ExerciseSessionMapper;
import com.raisedeveloper.server.domain.routine.domain.RoutineStep;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseSessionService {

	private final ExerciseSessionRepository exerciseSessionRepository;
	private final ExerciseResultRepository exerciseResultRepository;
	private final com.raisedeveloper.server.domain.routine.infra.RoutineStepRepository routineStepRepository;
	private final ExerciseSessionMapper exerciseSessionMapper;

	public ExerciseSessionResponse getExerciseSession(Long userId, Long sessionId) {
		ExerciseSession session = exerciseSessionRepository
			.findByIdAndUserIdWithRoutine(sessionId, userId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.EXERCISE_SESSION_NOT_FOUND)
			);

		List<RoutineStep> routineSteps = routineStepRepository
			.findByRoutineIdWithExercise(session.getRoutine().getId());

		log.info("Exercise session retrieved: sessionId={}, userId={}", sessionId, userId);

		return exerciseSessionMapper.toSessionResponse(session, routineSteps);
	}

	public ExerciseSessionValidListResponse getValidExerciseSessions(Long userId) {
		List<ExerciseSession> sessions = exerciseSessionRepository
			.findByUserIdAndIsRoutineCompletedIsNullOrderByCreatedAtDesc(userId);
		return exerciseSessionMapper.toValidListResponse(sessions);
	}

	@Transactional
	public ExerciseSession getSessionForUpdate(Long userId, Long sessionId) {
		return exerciseSessionRepository
			.findByIdAndUserIdWithRoutine(sessionId, userId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.EXERCISE_SESSION_NOT_FOUND)
			);
	}

	@Transactional
	public long updateSessionAndResults(ExerciseSession session, ExerciseSessionUpdateRequest request) {
		long completedCount = countCompletedSteps(request.exerciseResult());
		boolean hasSuccess = completedCount >= 1;
		session.updateSession(request.startAt(), request.endAt(), hasSuccess);

		List<ExerciseResult> existingResults = exerciseResultRepository
			.findByExerciseSessionIdWithDetails(session.getId());

		Map<Long, ExerciseResult> resultMap = existingResults.stream()
			.collect(Collectors.toMap(
				result -> result.getRoutineStep().getId(),
				result -> result
			));

		for (ExerciseResultRequest resultRequest : request.exerciseResult()) {
			ExerciseResult result = resultMap.get(resultRequest.routineStepId());
			if (result == null) {
				log.warn("ExerciseResult not found: sessionId={}, routineStepId={}",
					session.getId(), resultRequest.routineStepId());
				continue;
			}

			result.stepResultsUpdate(
				resultRequest.status(),
				resultRequest.accuracy().byteValue(),
				resultRequest.pose_record().toString(),
				resultRequest.startAt(),
				resultRequest.endAt()
			);
		}

		return completedCount;
	}

	public boolean hasCompletedSessionToday(Long userId) {
		java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));
		java.time.LocalDateTime startAt = today.atStartOfDay();
		java.time.LocalDateTime endAt = today.plusDays(1).atStartOfDay();
		return exerciseSessionRepository.existsCompletedInRange(userId, startAt, endAt);
	}

	private long countCompletedSteps(List<ExerciseResultRequest> results) {
		return results.stream()
			.filter(r -> r.status().name().equals("COMPLETED"))
			.count();
	}
}
