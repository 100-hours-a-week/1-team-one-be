package com.raisedeveloper.server.domain.user.api;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.user.application.UserService;
import com.raisedeveloper.server.domain.user.dto.AlarmSettingsDndRequest;
import com.raisedeveloper.server.domain.user.dto.AlarmSettingsDndResponse;
import com.raisedeveloper.server.domain.user.dto.AlarmSettingsRequest;
import com.raisedeveloper.server.domain.user.dto.CharacterCreateRequest;
import com.raisedeveloper.server.domain.user.dto.CharacterCreateResponse;
import com.raisedeveloper.server.domain.user.dto.OnboardingResponse;
import com.raisedeveloper.server.domain.user.dto.ProfileImageUpdateRequest;
import com.raisedeveloper.server.domain.user.dto.ProfileNicknameUpdateRequest;
import com.raisedeveloper.server.domain.user.dto.UserMeAlarmSettingsResponse;
import com.raisedeveloper.server.domain.user.dto.UserMeResponse;
import com.raisedeveloper.server.domain.user.dto.UserProfileResponse;
import com.raisedeveloper.server.global.response.ApiResponse;
import com.raisedeveloper.server.global.security.utils.AuthUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/me")
	public ApiResponse<UserMeResponse> getMe() {
		Long userId = AuthUtils.resolveUserIdFromContext();
		return ApiResponse.of("GET_ME_SUCCESS", userService.getMe(userId));
	}

	@DeleteMapping("/me")
	public ApiResponse<Object> withdraw() {
		Long userId = AuthUtils.resolveUserIdFromContext();
		userService.withdraw(userId);
		return ApiResponse.of("WITHDRAWAL_SUCCESS", Map.of());
	}

	@GetMapping("/me/onboarding-completed")
	public ApiResponse<OnboardingResponse> getOnboardingCompleted() {
		Long userId = AuthUtils.resolveUserIdFromContext();
		OnboardingResponse res = userService.checkUserOnboardingCompleted(userId);
		return ApiResponse.of("GET_ONBOARDING_COMPLETED", res);
	}

	@PostMapping("/me/onboarding-completed")
	public ApiResponse<Object> markOnboardingCompleted() {
		Long userId = AuthUtils.resolveUserIdFromContext();
		userService.markOnboardingCompleted(userId);
		return ApiResponse.of("GET_ONBOARDING_COMPLETED", Map.of());
	}

	@PatchMapping("/me/profile/image")
	public ApiResponse<UserProfileResponse> updateProfileImage(
		@Valid @RequestBody ProfileImageUpdateRequest request
	) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		UserProfileResponse response = userService.updateProfileImage(userId, request.imagePath());
		return ApiResponse.of("UPDATE_PROFILE_IMAGE_SUCCESS", response);
	}

	@PatchMapping("/me/profile/nickname")
	public ApiResponse<UserProfileResponse> updateNickname(
		@Valid @RequestBody ProfileNicknameUpdateRequest request
	) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		UserProfileResponse response = userService.updateNickname(userId, request.nickname());
		return ApiResponse.of("UPDATE_NICKNAME_SUCCESS", response);
	}

	@GetMapping("/me/alarm-settings")
	public ApiResponse<UserMeAlarmSettingsResponse> getAlarmSettings() {
		Long userId = AuthUtils.resolveUserIdFromContext();
		return ApiResponse.of("GET_ME_ALARM_SETTING_SUCCESS", userService.getAlarmSettings(userId));
	}

	@GetMapping("/me/alarm-settings/dnd")
	public ApiResponse<AlarmSettingsDndResponse> getAlarmDnd() {
		Long userId = AuthUtils.resolveUserIdFromContext();
		return ApiResponse.of("GET_ME_ALARM_SETTING_DND_SUCCESS", userService.getAlarmDnd(userId));
	}

	@PutMapping("/me/alarm-settings")
	public ApiResponse<Object> setAlarmSettings(@Valid @RequestBody AlarmSettingsRequest request) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		userService.setAlarmSettings(userId, request);
		return ApiResponse.of("SET_ME_ALARM_SETTING_SUCCESS", Map.of());
	}

	@PutMapping("/me/alarm-settings/dnd")
	public ApiResponse<Object> updateAlarmDnd(@Valid @RequestBody AlarmSettingsDndRequest request) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		userService.updateAlarmDnd(userId, request);
		return ApiResponse.of("SET_ME_ALARM_SETTING_DND_SUCCESS", Map.of());
	}

	@PostMapping("/me/character")
	public ApiResponse<CharacterCreateResponse> createCharacter(@Valid @RequestBody CharacterCreateRequest request) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		return ApiResponse.of("CREATE_CHARACTER_SUCCESS", userService.createCharacter(userId, request));
	}

	@GetMapping("/{userId}")
	public ApiResponse<UserMeResponse> getUserProfile(@PathVariable Long userId) {
		return ApiResponse.of("GET_USER_PROFILE_SUCCESS", userService.getUserProfile(userId));
	}
}
