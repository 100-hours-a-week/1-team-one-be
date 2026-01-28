package com.raisedeveloper.server.domain.exercise.dto;

public record QuestProgressDto(
	Long id,
	String name,
	int targetCount,
	int currentCount
) {
}
