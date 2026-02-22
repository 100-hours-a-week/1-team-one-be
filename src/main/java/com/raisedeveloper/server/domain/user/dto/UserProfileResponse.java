package com.raisedeveloper.server.domain.user.dto;

public record UserProfileResponse(
	String nickname,
	String imagePath
) {
}
