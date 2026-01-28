package com.raisedeveloper.server.domain.exercise.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.raisedeveloper.server.domain.common.utils.JsonConverter;
import com.raisedeveloper.server.domain.exercise.domain.Exercise;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseType;

public record ExerciseResponse(
	Long id,
	String name,
	String content,
	String effect,
	ExerciseType type,
	JsonNode pose,
	String bodyPart,
	byte difficulty,
	String tags
) {

	public static ExerciseResponse from(Exercise exercise) {

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
}
