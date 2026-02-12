package com.raisedeveloper.server.domain.post.dto;

import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.domain.UserProfile;

public record PostAuthor(
	Long userId,
	String profileImageUrl,
	String nickname,
	int level,
	int streak
) {

	public static PostAuthor from(Long authorId, UserProfile profile, UserCharacter character) {
		return new PostAuthor(
			authorId,
			profile.getImagePath(),
			profile.getNickname(),
			character.getLevel(),
			character.getStreak()
		);
	}
}
