package com.raisedeveloper.server.domain.user.dto;

public record UserMeResponse(
	Long userId,
	UserProfileResponse profile,
	UserCharacterResponse character
) {
}
