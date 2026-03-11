package com.raisedeveloper.server.domain.satisfaction.application;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.Exercise;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.routine.domain.RoutineStep;
import com.raisedeveloper.server.domain.routine.infra.RoutineStepRepository;
import com.raisedeveloper.server.domain.satisfaction.client.AiSatisfactionClient;
import com.raisedeveloper.server.domain.satisfaction.domain.ExerciseSatisfaction;
import com.raisedeveloper.server.domain.satisfaction.domain.RoutineSatisfaction;
import com.raisedeveloper.server.domain.satisfaction.dto.AiExerciseSatisfactionDto;
import com.raisedeveloper.server.domain.satisfaction.dto.AiExerciseSatisfactionSyncRequest;
import com.raisedeveloper.server.domain.satisfaction.dto.SatisfactionVoteRequest;
import com.raisedeveloper.server.domain.satisfaction.dto.SatisfactionVoteResponse;
import com.raisedeveloper.server.domain.satisfaction.infra.ExerciseSatisfactionRepository;
import com.raisedeveloper.server.domain.satisfaction.infra.RoutineSatisfactionRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SatisfactionService {

	private final ExerciseSessionRepository exerciseSessionRepository;
	private final RoutineStepRepository routineStepRepository;
	private final RoutineSatisfactionRepository routineSatisfactionRepository;
	private final ExerciseSatisfactionRepository exerciseSatisfactionRepository;
	private final AiSatisfactionClient aiSatisfactionClient;

	public SatisfactionVoteResponse vote(Long userId, Long sessionId, SatisfactionVoteRequest request) {
		ExerciseSession session = exerciseSessionRepository.findByIdAndUserIdWithRoutine(sessionId, userId)
			.orElseThrow(() -> new CustomException(ErrorCode.EXERCISE_SESSION_NOT_FOUND));

		validateVoteTarget(session, request.routineId());

		byte satisfaction = request.satisfied() ? (byte)1 : (byte)-1;
		upsertRoutineSatisfaction(session, satisfaction);
		upsertExerciseSatisfactions(session, satisfaction);

		return new SatisfactionVoteResponse(session.getId(), session.getRoutine().getId(), satisfaction);
	}

	@Transactional(readOnly = true)
	public void syncAllExerciseSatisfactions() {
		List<AiExerciseSatisfactionDto> satisfactions = exerciseSatisfactionRepository.findAllForAiSync()
			.stream()
			.map(row -> new AiExerciseSatisfactionDto(
				row.getUserId(),
				row.getExerciseId(),
				row.getSatisfaction()
			))
			.toList();

		if (satisfactions.isEmpty()) {
			return;
		}

		aiSatisfactionClient.updateSatisfactions(new AiExerciseSatisfactionSyncRequest(satisfactions));
	}

	private void validateVoteTarget(ExerciseSession session, Long routineId) {
		if (session.getIsRoutineCompleted() == null) {
			throw validationException("exerciseSessionId", EXERCISE_SESSION_SATISFACTION_UNAVAILABLE_MESSAGE);
		}

		if (!session.getRoutine().getId().equals(routineId)) {
			throw validationException("routineId", EXERCISE_SESSION_SATISFACTION_ROUTINE_MISMATCH_MESSAGE);
		}
	}

	private void upsertRoutineSatisfaction(ExerciseSession session, byte satisfaction) {
		RoutineSatisfaction routineSatisfaction = routineSatisfactionRepository
			.findByUserIdAndRoutineId(session.getUser().getId(), session.getRoutine().getId())
			.orElseGet(() -> new RoutineSatisfaction(session.getUser(), session.getRoutine(), satisfaction));

		routineSatisfaction.updateSatisfaction(satisfaction);
		routineSatisfactionRepository.save(routineSatisfaction);
	}

	private void upsertExerciseSatisfactions(ExerciseSession session, byte satisfaction) {
		Map<Long, Exercise> exercisesById = routineStepRepository.findByRoutineIdWithExercise(
				session.getRoutine().getId())
			.stream()
			.map(RoutineStep::getExercise)
			.collect(Collectors.toMap(
				Exercise::getId,
				Function.identity(),
				(existing, ignored) -> existing,
				LinkedHashMap::new
			));

		if (exercisesById.isEmpty()) {
			return;
		}

		Map<Long, ExerciseSatisfaction> existingByExerciseId = exerciseSatisfactionRepository
			.findAllByUserIdAndExerciseIdIn(session.getUser().getId(), exercisesById.keySet())
			.stream()
			.collect(java.util.stream.Collectors.toMap(
				exerciseSatisfaction -> exerciseSatisfaction.getExercise().getId(),
				Function.identity()
			));

		List<ExerciseSatisfaction> exerciseSatisfactions = exercisesById.values().stream()
			.map(exercise -> {
				ExerciseSatisfaction exerciseSatisfaction = existingByExerciseId.get(exercise.getId());
				if (exerciseSatisfaction == null) {
					return new ExerciseSatisfaction(session.getUser(), exercise, satisfaction);
				}
				exerciseSatisfaction.updateSatisfaction(satisfaction);
				return exerciseSatisfaction;
			})
			.toList();

		exerciseSatisfactionRepository.saveAll(exerciseSatisfactions);
	}

	private CustomException validationException(String field, String reason) {
		return new CustomException(ErrorCode.VALIDATION_FAILED, List.of(ErrorDetail.field(field, reason)));
	}
}
