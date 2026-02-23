package com.raisedeveloper.server.domain.routine.application;

import static com.raisedeveloper.server.domain.exercise.domain.ExerciseType.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.common.enums.RoutineGenerationJobStatus;
import com.raisedeveloper.server.domain.common.enums.RoutineStatus;
import com.raisedeveloper.server.domain.exercise.domain.Exercise;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseType;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseRepository;
import com.raisedeveloper.server.domain.routine.client.dto.AiRoutineDto;
import com.raisedeveloper.server.domain.routine.client.dto.AiRoutineResponse;
import com.raisedeveloper.server.domain.routine.client.dto.AiRoutineStepDto;
import com.raisedeveloper.server.domain.routine.domain.Routine;
import com.raisedeveloper.server.domain.routine.domain.RoutineGenerationJob;
import com.raisedeveloper.server.domain.routine.domain.RoutineStep;
import com.raisedeveloper.server.domain.routine.dto.RoutineExerciseResponse;
import com.raisedeveloper.server.domain.routine.dto.RoutinePlanResponse;
import com.raisedeveloper.server.domain.routine.infra.RoutineGenerationJobRepository;
import com.raisedeveloper.server.domain.routine.infra.RoutineRepository;
import com.raisedeveloper.server.domain.routine.infra.RoutineStepRepository;
import com.raisedeveloper.server.domain.survey.domain.SurveySubmission;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineService {

	private final RoutineRepository routineRepository;
	private final RoutineStepRepository routineStepRepository;
	private final RoutineGenerationJobRepository routineGenerationJobRepository;
	private final UserRepository userRepository;
	private final ExerciseRepository exerciseRepository;

	public RoutinePlanResponse getMyRoutine(Long userId) {
		userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)
		);

		List<Routine> routines = routineRepository.findAllByUserIdAndIsActiveTrue(userId);

		Long activeSubmissionId = null;
		List<RoutineExerciseResponse> exercises = List.of();

		if (!routines.isEmpty()) {
			List<Long> routineIds = routines.stream().map(Routine::getId).toList();
			List<RoutineStep> steps = routineStepRepository
				.findAllByRoutineIdInOrderByIdAsc(routineIds);

			steps.sort(Comparator.comparingInt(s -> s.getRoutine().getRoutineOrder()));

			Map<Long, RoutineExerciseResponse> uniqueExercises = new LinkedHashMap<>();
			Routine primaryRoutine = null;

			for (RoutineStep step : steps) {
				Exercise exercise = step.getExercise();
				Routine routine = step.getRoutine();

				if (primaryRoutine == null) {
					primaryRoutine = routine;
				}

				uniqueExercises.putIfAbsent(
					exercise.getId(),
					toExerciseResponse(exercise, routine.getReason())
				);
			}

			if (primaryRoutine != null) {
				activeSubmissionId = primaryRoutine.getSurveySubmission().getId();
			}
			exercises = List.copyOf(uniqueExercises.values());
		}

		RoutineGenerationJob latestJob = routineGenerationJobRepository
			.findTopByUserIdOrderByIdDesc(userId)
			.orElse(null);

		if (latestJob == null) {
			if (routines.isEmpty()) {
				throw new CustomException(ErrorCode.ROUTINE_NOT_FOUND);
			}
			return new RoutinePlanResponse(
				RoutineGenerationJobStatus.COMPLETED,
				activeSubmissionId,
				activeSubmissionId,
				exercises
			);
		}

		Long generatingSubmissionId = latestJob.getSurveySubmission().getId();
		RoutineGenerationJobStatus status = latestJob.getStatus();

		return new RoutinePlanResponse(
			status,
			activeSubmissionId,
			generatingSubmissionId,
			exercises
		);
	}

	private RoutineExerciseResponse toExerciseResponse(Exercise exercise, String reason) {
		return new RoutineExerciseResponse(
			exercise.getId(),
			exercise.getName(),
			exercise.getContent(),
			reason
		);
	}

	@Transactional
	public void createRoutinesFromAiResponse(
		User user,
		SurveySubmission submission,
		AiRoutineResponse aiResponse
	) {
		validateAiResponse(aiResponse);

		List<Routine> existingRoutines = routineRepository.findAllByUserIdAndIsActiveTrue(user.getId());
		existingRoutines.forEach(Routine::routineInactivated);
		routineRepository.saveAll(existingRoutines);

		for (AiRoutineDto routineDto : aiResponse.routines()) {
			Routine routine = new Routine(
				user,
				submission,
				routineDto.routineOrder().shortValue(),
				RoutineStatus.COMPLETED,
				routineDto.reason()
			);
			routineRepository.save(routine);

			for (AiRoutineStepDto stepDto : routineDto.steps()) {
				Exercise exercise = findExerciseById(stepDto.exerciseId());

				validateExerciseType(exercise, stepDto);

				validateStepFields(stepDto);

				RoutineStep step = new RoutineStep(
					routine,
					exercise,
					stepDto.targetReps() != null ? stepDto.targetReps().shortValue() : null,
					stepDto.durationTime() != null ? stepDto.durationTime().shortValue() : null,
					stepDto.limitTime().shortValue(),
					stepDto.stepOrder().shortValue()
				);
				routineStepRepository.save(step);
			}
		}
	}

	private void validateAiResponse(AiRoutineResponse response) {
		if (response.status() != RoutineGenerationJobStatus.COMPLETED) {
			throw new CustomException(
				ErrorCode.AI_ROUTINE_NOT_COMPLETED,
				List.of(ErrorDetail.field("status", response.status().toString()))
			);
		}

		if (!response.hasRoutines()) {
			throw new CustomException(ErrorCode.AI_ROUTINE_EMPTY);
		}

		for (AiRoutineDto routine : response.routines()) {
			if (routine.steps() == null || routine.steps().isEmpty()) {
				throw new CustomException(
					ErrorCode.AI_ROUTINE_STEPS_EMPTY,
					List.of(ErrorDetail.field("routineOrder", String.valueOf(routine.routineOrder())))
				);
			}
		}
	}

	private Exercise findExerciseById(String exerciseId) {
		try {
			Long id = Long.parseLong(exerciseId);
			return exerciseRepository.findByIdAndIsDeprecatedFalse(id)
				.orElseThrow(() -> new CustomException(
					ErrorCode.EXERCISE_NOT_FOUND,
					List.of(ErrorDetail.field("exerciseId", exerciseId))
				));
		} catch (NumberFormatException e) {
			throw new CustomException(
				ErrorCode.EXERCISE_NOT_FOUND,
				List.of(ErrorDetail.field("exerciseId", exerciseId))
			);
		}
	}

	private void validateExerciseType(Exercise exercise, AiRoutineStepDto stepDto) {
		ExerciseType exerciseType = exercise.getType();
		if (!exerciseType.equals(stepDto.type())) {
			throw new CustomException(
				ErrorCode.EXERCISE_TYPE_MISMATCH,
				List.of(
					ErrorDetail.field("exerciseId", stepDto.exerciseId()),
					ErrorDetail.field("expectedType", exerciseType.name()),
					ErrorDetail.field("actualType", stepDto.type().name())
				)
			);
		}
	}

	private void validateStepFields(AiRoutineStepDto stepDto) {
		if (stepDto.exerciseId() == null || stepDto.exerciseId().isBlank()) {
			throw new CustomException(ErrorCode.AI_ROUTINE_EXERCISE_ID_MISSING);
		}

		if (REPS.equals(stepDto.type()) && stepDto.targetReps() == null) {
			throw new CustomException(
				ErrorCode.AI_ROUTINE_TARGET_REPS_MISSING,
				List.of(ErrorDetail.field("exerciseId", stepDto.exerciseId()))
			);
		}

		if (DURATION.equals(stepDto.type()) && stepDto.durationTime() == null) {
			throw new CustomException(
				ErrorCode.AI_ROUTINE_DURATION_TIME_MISSING,
				List.of(ErrorDetail.field("exerciseId", stepDto.exerciseId()))
			);
		}
	}
}
