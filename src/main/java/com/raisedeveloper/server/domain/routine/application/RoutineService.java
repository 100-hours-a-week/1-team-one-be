package com.raisedeveloper.server.domain.routine.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.Exercise;
import com.raisedeveloper.server.domain.routine.domain.Routine;
import com.raisedeveloper.server.domain.routine.domain.RoutineStep;
import com.raisedeveloper.server.domain.routine.dto.RoutineExerciseResponse;
import com.raisedeveloper.server.domain.routine.dto.RoutinePlanResponse;
import com.raisedeveloper.server.domain.routine.infra.RoutineRepository;
import com.raisedeveloper.server.domain.routine.infra.RoutineStepRepository;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineService {

	private final RoutineRepository routineRepository;
	private final RoutineStepRepository routineStepRepository;
	private final UserRepository userRepository;

	public RoutinePlanResponse getMyRoutine(Long userId) {
		userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)
		);

		List<Routine> routines = routineRepository.findAllByUserIdAndIsActiveTrue(userId);
		if (routines.isEmpty()) {
			throw new CustomException(ErrorCode.ROUTINE_NOT_FOUND);
		}

		List<Long> routineIds = routines.stream().map(Routine::getId).toList();
		List<RoutineStep> steps = routineStepRepository
			.findAllByRoutineIdIn(routineIds);
		Map<Long, RoutineExerciseResponse> uniqueExercises = new LinkedHashMap<>();
		for (RoutineStep step : steps) {
			Exercise exercise = step.getExercise();
			uniqueExercises.putIfAbsent(
				exercise.getId(),
				toExerciseResponse(exercise, step.getReason())
			);
		}

		Routine primaryRoutine = routines.get(0);
		return new RoutinePlanResponse(
			primaryRoutine.getStatus(),
			primaryRoutine.getId(),
			List.copyOf(uniqueExercises.values())
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
}
