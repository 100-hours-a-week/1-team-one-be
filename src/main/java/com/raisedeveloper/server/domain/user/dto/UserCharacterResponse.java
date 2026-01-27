package com.raisedeveloper.server.domain.user.dto;

public record UserCharacterResponse(
	String type,
	String name,
	short level,
	int exp,
	int streak,
	int statusScore
) {
}
