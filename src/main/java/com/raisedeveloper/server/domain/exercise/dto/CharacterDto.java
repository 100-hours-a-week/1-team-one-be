package com.raisedeveloper.server.domain.exercise.dto;

public record CharacterDto(
	int level,
	int exp,
	int streak,
	int statusScore
) {
}
