package com.raisedeveloper.server.domain.satisfaction.dto;

public record AiExerciseSatisfactionDto(
	Long userId,
	Long exerciseId,
	Byte satisfaction
) {
}
