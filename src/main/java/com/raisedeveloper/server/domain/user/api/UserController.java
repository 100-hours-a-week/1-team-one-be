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
import com.raisedeveloper.server.global.security.currentuser.CurrentUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/me")
	public ApiResponse<UserMeResponse> getMe(@CurrentUser Long userId) {
		return ApiResponse.of("GET_ME_SUCCESS", userService.getMe(userId));
	}

	@DeleteMapping("/me")
	public ApiResponse<Object> withdraw(@CurrentUser Long userId) {
		userService.withdraw(userId);
		return ApiResponse.of("WITHDRAWAL_SUCCESS", Map.of());
	}

	@GetMapping("/me/onboarding-completed")
	public ApiResponse<OnboardingResponse> getOnboardingCompleted(@CurrentUser Long userId) {
		OnboardingResponse res = userService.checkUserOnboardingCompleted(userId);
		return ApiResponse.of("GET_ONBOARDING_COMPLETED", res);
	}

	@PostMapping("/me/onboarding-completed")
	public ApiResponse<Object> markOnboardingCompleted(@CurrentUser Long userId) {
		userService.markOnboardingCompleted(userId);
		return ApiResponse.of("GET_ONBOARDING_COMPLETED", Map.of());
	}

	@PatchMapping("/me/profile/image")
	public ApiResponse<UserProfileResponse> updateProfileImage(
		@CurrentUser Long userId,
		@Valid @RequestBody ProfileImageUpdateRequest request
	) {
		UserProfileResponse response = userService.updateProfileImage(userId, request.imagePath());
		return ApiResponse.of("UPDATE_PROFILE_IMAGE_SUCCESS", response);
	}

	@PatchMapping("/me/profile/nickname")
	public ApiResponse<UserProfileResponse> updateNickname(
		@CurrentUser Long userId,
		@Valid @RequestBody ProfileNicknameUpdateRequest request
	) {
		UserProfileResponse response = userService.updateNickname(userId, request.nickname());
		return ApiResponse.of("UPDATE_NICKNAME_SUCCESS", response);
	}

	@GetMapping("/me/alarm-settings")
	public ApiResponse<UserMeAlarmSettingsResponse> getAlarmSettings(@CurrentUser Long userId) {
		return ApiResponse.of("GET_ME_ALARM_SETTING_SUCCESS", userService.getAlarmSettings(userId));
	}

	@GetMapping("/me/alarm-settings/dnd")
	public ApiResponse<AlarmSettingsDndResponse> getAlarmDnd(@CurrentUser Long userId) {
		return ApiResponse.of("GET_ME_ALARM_SETTING_DND_SUCCESS", userService.getAlarmDnd(userId));
	}

	@PutMapping("/me/alarm-settings")
	public ApiResponse<Object> setAlarmSettings(
		@CurrentUser Long userId,
		@Valid @RequestBody AlarmSettingsRequest request
	) {
		userService.setAlarmSettings(userId, request);
		return ApiResponse.of("SET_ME_ALARM_SETTING_SUCCESS", Map.of());
	}

	@PutMapping("/me/alarm-settings/dnd")
	public ApiResponse<Object> updateAlarmDnd(
		@CurrentUser Long userId,
		@Valid @RequestBody AlarmSettingsDndRequest request
	) {
		userService.updateAlarmDnd(userId, request);
		return ApiResponse.of("SET_ME_ALARM_SETTING_DND_SUCCESS", Map.of());
	}

	@PostMapping("/me/character")
	public ApiResponse<CharacterCreateResponse> createCharacter(
		@CurrentUser Long userId,
		@Valid @RequestBody CharacterCreateRequest request
	) {
		return ApiResponse.of("CREATE_CHARACTER_SUCCESS", userService.createCharacter(userId, request));
	}

	@GetMapping("/{userId}")
	public ApiResponse<UserMeResponse> getUserProfile(@PathVariable Long userId) {
		return ApiResponse.of("GET_USER_PROFILE_SUCCESS", userService.getUserProfile(userId));
	}
}
