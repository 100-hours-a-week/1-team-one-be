package com.raisedeveloper.server.domain.user.mapper;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.domain.UserProfile;
import com.raisedeveloper.server.domain.user.dto.AlarmSettingsDndResponse;
import com.raisedeveloper.server.domain.user.dto.AlarmSettingsResponse;
import com.raisedeveloper.server.domain.user.dto.UserCharacterResponse;
import com.raisedeveloper.server.domain.user.dto.UserMeAlarmSettingsResponse;
import com.raisedeveloper.server.domain.user.dto.UserMeResponse;
import com.raisedeveloper.server.domain.user.dto.UserProfileResponse;

@Component
public class UserMapper {

	public UserMeResponse toMeResponse(User user, UserProfile profile, UserCharacter character) {
		return new UserMeResponse(
			user.getId(),
			toProfileResponse(profile),
			toCharacterResponse(character)
		);
	}

	public UserProfileResponse toProfileResponse(UserProfile profile) {
		return new UserProfileResponse(profile.getNickname(), profile.getImagePath());
	}

	public UserCharacterResponse toCharacterResponse(UserCharacter character) {
		return new UserCharacterResponse(
			character.getType().name(),
			character.getName(),
			character.getLevel(),
			character.getExp(),
			character.getStreak(),
			character.getStatusScore()
		);
	}

	public UserMeAlarmSettingsResponse toAlarmSettingsResponse(UserAlarmSettings settings) {
		List<String> repeatDays = settings.getRepeatDays() == null || settings.getRepeatDays().isBlank()
			? List.of()
			: Arrays.asList(settings.getRepeatDays().split(","));

		return new UserMeAlarmSettingsResponse(
			new AlarmSettingsResponse(
				settings.getAlarmInterval(),
				settings.getActiveStartAt(),
				settings.getActiveEndAt(),
				settings.getFocusStartAt(),
				settings.getFocusEndAt(),
				repeatDays
			)
		);
	}

	public AlarmSettingsDndResponse toAlarmSettingsDndResponse(UserAlarmSettings settings) {
		return new AlarmSettingsDndResponse(settings.isDnd(), settings.getDndFinishedAt());
	}
}
