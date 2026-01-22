package com.raisedeveloper.server.domain.user.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.auth.application.AuthService;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.domain.UserProfile;
import com.raisedeveloper.server.domain.user.dto.AlarmSettingsDndRequest;
import com.raisedeveloper.server.domain.user.dto.AlarmSettingsRequest;
import com.raisedeveloper.server.domain.user.dto.CharacterCreateRequest;
import com.raisedeveloper.server.domain.user.dto.CharacterCreateResponse;
import com.raisedeveloper.server.domain.user.dto.UserMeAlarmSettingsResponse;
import com.raisedeveloper.server.domain.user.dto.UserMeResponse;
import com.raisedeveloper.server.domain.user.infra.UserAlarmSettingsRepository;
import com.raisedeveloper.server.domain.user.infra.UserCharacterRepository;
import com.raisedeveloper.server.domain.user.infra.UserProfileRepository;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final AuthService authService;
	private final UserRepository userRepository;
	private final UserAlarmSettingsRepository userAlarmSettingsRepository;
	private final UserCharacterRepository userCharacterRepository;
	private final UserProfileRepository userProfileRepository;

	@Transactional
	public UserMeResponse getMe(Long userId) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)
		);
		UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)
		);
		UserCharacter character = userCharacterRepository.findByUserId(userId).orElseThrow(
			() -> new CustomException(ErrorCode.CHARACTER_NOT_SET)
		);
		return UserMeResponse.from(user, profile, character);
	}

	@Transactional
	public void withdraw(Long userId) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)
		);
		user.softDelete(LocalDateTime.now());
		authService.logoutAll(userId);
	}

	@Transactional
	public UserMeAlarmSettingsResponse getAlarmSettings(Long userId) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)
		);

		UserAlarmSettings settings = userAlarmSettingsRepository.findByUserId(userId)
			.orElseThrow(
				() -> new CustomException(ErrorCode.ALARM_SETTING_NOT_FOUND));

		return UserMeAlarmSettingsResponse.from(settings);
	}

	@Transactional
	public void setAlarmSettings(Long userId, AlarmSettingsRequest request) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)
		);

		String repeatDays = String.join(",", request.repeatDays());
		userAlarmSettingsRepository.findByUserId(userId)
			.ifPresentOrElse(
				existing -> existing.updateSettings(
					request.interval(),
					request.activeStartAt(),
					request.activeEndAt(),
					request.focusStartAt(),
					request.focusEndAt(),
					repeatDays,
					request.dndFinishedAt()
				),
				() -> userAlarmSettingsRepository.save(new UserAlarmSettings(
					user,
					request.interval(),
					request.activeStartAt(),
					request.activeEndAt(),
					request.focusStartAt(),
					request.focusEndAt(),
					repeatDays,
					request.dndFinishedAt()
				))
			);
	}

	@Transactional
	public void updateAlarmDnd(Long userId, AlarmSettingsDndRequest request) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)
		);

		UserAlarmSettings alarmSettings = userAlarmSettingsRepository.findByUserId(user.getId())
			.orElseThrow(
				() -> new CustomException(ErrorCode.ALARM_SETTING_NOT_FOUND)
			);

		alarmSettings.enableDnd(request.dndFinishedAt());
	}

	@Transactional
	public CharacterCreateResponse createCharacter(Long userId, CharacterCreateRequest request) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)
		);

		if (userCharacterRepository.findByUserId(userId).isPresent()) {
			throw new CustomException(ErrorCode.CHARACTER_ALREADY_SET);
		}

		UserCharacter character = userCharacterRepository.save(new UserCharacter(user, request.type()));
		return new CharacterCreateResponse(character.getId());
	}
}
