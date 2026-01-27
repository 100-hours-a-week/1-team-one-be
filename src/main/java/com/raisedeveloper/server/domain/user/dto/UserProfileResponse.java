package com.raisedeveloper.server.domain.user.dto;

import com.raisedeveloper.server.domain.user.domain.UserProfile;

public record UserProfileResponse(
	String nickname,
	String imagePath
) {
	public static UserProfileResponse from(UserProfile profile) {
		return new UserProfileResponse(profile.getNickname(), profile.getImagePath());
	}
}
