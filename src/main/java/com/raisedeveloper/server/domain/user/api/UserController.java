package com.raisedeveloper.server.domain.user.api;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.user.application.UserService;
import com.raisedeveloper.server.domain.user.dto.AlarmSettingsDndRequest;
import com.raisedeveloper.server.domain.user.dto.AlarmSettingsRequest;
import com.raisedeveloper.server.domain.user.dto.CharacterCreateRequest;
import com.raisedeveloper.server.domain.user.dto.CharacterCreateResponse;
import com.raisedeveloper.server.domain.user.dto.UserMeAlarmSettingsResponse;
import com.raisedeveloper.server.domain.user.dto.UserMeResponse;
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
		return ApiResponse.success("GET_ME_SUCCESS", userService.getMe(userId));
	}

	@DeleteMapping("/me")
	public ApiResponse<Object> withdraw() {
		Long userId = AuthUtils.resolveUserIdFromContext();
		userService.withdraw(userId);
		return ApiResponse.success("WITHDRAWAL_SUCCESS", Map.of());
	}

	@GetMapping("/me/alarm-settings")
	public ApiResponse<UserMeAlarmSettingsResponse> getAlarmSettings() {
		Long userId = AuthUtils.resolveUserIdFromContext();
		return ApiResponse.success("GET_ME_ALARM_SETTING_SUCCESS", userService.getAlarmSettings(userId));
	}

	@PutMapping("/me/alarm-settings")
	public ApiResponse<Object> setAlarmSettings(@Valid @RequestBody AlarmSettingsRequest request) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		userService.setAlarmSettings(userId, request);
		return ApiResponse.success("SET_ME_ALARM_SETTING_SUCCESS", Map.of());
	}

	@PutMapping("/me/alarm-settings/dnd")
	public ApiResponse<Object> updateAlarmDnd(@Valid @RequestBody AlarmSettingsDndRequest request) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		userService.updateAlarmDnd(userId, request);
		return ApiResponse.success("SET_ME_ALARM_SETTING_DND_SUCCESS", Map.of());
	}

	@PostMapping("/me/character")
	public ApiResponse<CharacterCreateResponse> createCharacter(@Valid @RequestBody CharacterCreateRequest request) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		return ApiResponse.success("CREATE_CHARACTER_SUCCESS", userService.createCharacter(userId, request));
	}
}
