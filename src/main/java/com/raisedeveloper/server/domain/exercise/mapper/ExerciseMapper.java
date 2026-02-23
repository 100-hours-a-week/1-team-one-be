package com.raisedeveloper.server.domain.exercise.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.common.utils.JsonConverter;
import com.raisedeveloper.server.domain.exercise.domain.Exercise;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseListResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseResponse;
import com.raisedeveloper.server.domain.exercise.dto.RoutineStepResponse;
import com.raisedeveloper.server.domain.routine.domain.RoutineStep;

@Component
public class ExerciseMapper {

	public ExerciseResponse toExerciseResponse(Exercise exercise) {
		return new ExerciseResponse(
			exercise.getId(),
			exercise.getName(),
			exercise.getContent(),
			exercise.getEffect(),
			exercise.getType(),
			JsonConverter.from(exercise.getPose()),
			exercise.getBodyPart(),
			exercise.getDifficulty(),
			exercise.getTags()
		);
	}

	public List<ExerciseResponse> toExerciseResponses(List<Exercise> exercises) {
		return exercises.stream()
			.map(this::toExerciseResponse)
			.toList();
	}

	public ExerciseListResponse toExerciseListResponse(List<Exercise> exercises) {
		return new ExerciseListResponse(toExerciseResponses(exercises));
	}

	public RoutineStepResponse toRoutineStepResponse(RoutineStep routineStep) {
		return new RoutineStepResponse(
			routineStep.getId(),
			routineStep.getStepOrder(),
			routineStep.getTargetReps(),
			routineStep.getDurationTime(),
			routineStep.getLimitTime(),
			toExerciseResponse(routineStep.getExercise())
		);
	}

	public List<RoutineStepResponse> toRoutineStepResponses(List<RoutineStep> routineSteps) {
		return routineSteps.stream()
			.map(this::toRoutineStepResponse)
			.toList();
	}
}
