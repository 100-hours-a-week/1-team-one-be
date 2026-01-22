package com.raisedeveloper.server.domain.user.dto;

import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.domain.UserProfile;

public record UserMeResponse(
	Long userId,
	UserProfileResponse profile,
	UserCharacterResponse character
) {
	public static UserMeResponse from(User user, UserProfile profile, UserCharacter character) {
		UserCharacterResponse userCharacterResponse = new UserCharacterResponse(
			character.getType().name(),
			character.getName(),
			character.getLevel(),
			character.getExp(),
			character.getStreak(),
			character.getStatusScore()
		);

		return new UserMeResponse(
			user.getId(),
			new UserProfileResponse(profile.getNickname(), profile.getImagePath()),
			userCharacterResponse
		);
	}
}
