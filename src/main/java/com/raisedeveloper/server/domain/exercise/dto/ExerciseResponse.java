package com.raisedeveloper.server.domain.exercise.dto;

import com.fasterxml.jackson.databind.JsonNode;
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
}
