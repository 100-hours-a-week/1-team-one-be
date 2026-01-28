package com.raisedeveloper.server.domain.exercise.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseResult;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.dto.CharacterDto;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseResultRequest;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionCompleteResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionUpdateRequest;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseResultRepository;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.notification.application.NotificationService;
import com.raisedeveloper.server.domain.routine.domain.RoutineStep;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;
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
	private final com.raisedeveloper.server.domain.user.infra.UserCharacterRepository userCharacterRepository;
	private final NotificationService notificationService;

	public ExerciseSessionResponse getExerciseSession(Long userId, Long sessionId) {
		ExerciseSession session = exerciseSessionRepository
			.findByIdAndUserIdWithRoutine(sessionId, userId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.EXERCISE_SESSION_NOT_FOUND)
			);

		List<RoutineStep> routineSteps = routineStepRepository
			.findByRoutineIdWithExercise(session.getRoutine().getId());

		log.info("Exercise session retrieved: sessionId={}, userId={}", sessionId, userId);

		return ExerciseSessionResponse.of(session, routineSteps);
	}

	@Transactional
	public ExerciseSessionCompleteResponse completeExerciseSession(
		Long userId,
		Long sessionId,
		ExerciseSessionUpdateRequest request
	) {

		ExerciseSession session = exerciseSessionRepository
			.findByIdAndUserIdWithRoutine(sessionId, userId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.EXERCISE_SESSION_NOT_FOUND)
			);

		session.updateSession(request.startAt(), request.endAt(), true);

		List<ExerciseResult> existingResults = exerciseResultRepository
			.findByExerciseSessionIdWithDetails(sessionId);

		Map<Long, ExerciseResult> resultMap = existingResults.stream()
			.collect(Collectors.toMap(
				result -> result.getRoutineStep().getId(),
				result -> result
			));

		for (ExerciseResultRequest resultRequest : request.exerciseResult()) {
			ExerciseResult result = resultMap.get(resultRequest.routineStepId());
			if (result == null) {
				log.warn("ExerciseResult not found: sessionId={}, routineStepId={}",
					sessionId, resultRequest.routineStepId());
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

		int earnedExp = calculateEarnedExp(request.exerciseResult());
		int earnedStatusScore = calculateEarnedStatusScore(request.exerciseResult());

		UserCharacter character = userCharacterRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHARACTER_NOT_SET));

		character.addExp(earnedExp);
		character.addStatusScore(earnedStatusScore);
		boolean hasCompletedToday = hasCompletedSessionToday(userId);
		if (!hasCompletedToday) {
			character.incrementStreak();
		}

		CharacterDto characterDto = new CharacterDto(
			character.getLevel(),
			character.getExp(),
			character.getStreak(),
			character.getStatusScore()
		);

		log.info("Exercise session completed: sessionId={}, userId={}, earnedExp={}, earnedStatusScore={}",
			sessionId, userId, earnedExp, earnedStatusScore);

		notificationService.createStretchingSuccess(session.getUser(), earnedExp);

		return new ExerciseSessionCompleteResponse(
			sessionId,
			true,
			earnedExp,
			earnedStatusScore,
			characterDto,
			List.of() // TODO: Quest 기능 구현 시 실제 퀘스트 진행도 반환
		);
	}

	private int calculateEarnedExp(List<ExerciseResultRequest> results) {
		return (int)results.stream()
			.filter(r -> r.status().name().equals("COMPLETED"))
			.count() * 10;
	}

	private int calculateEarnedStatusScore(List<ExerciseResultRequest> results) {
		return (int)results.stream()
			.filter(r -> r.status().name().equals("COMPLETED"))
			.count();
	}

	private boolean hasCompletedSessionToday(Long userId) {
		java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));
		java.time.LocalDateTime startAt = today.atStartOfDay();
		java.time.LocalDateTime endAt = today.plusDays(1).atStartOfDay();
		return exerciseSessionRepository.existsCompletedInRange(userId, startAt, endAt);
	}
}
