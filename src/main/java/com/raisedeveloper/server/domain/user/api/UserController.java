package com.raisedeveloper.server.domain.user.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.user.application.UserService;
import com.raisedeveloper.server.domain.user.dto.CharacterCreateRequest;
import com.raisedeveloper.server.domain.user.dto.CharacterCreateResponse;
import com.raisedeveloper.server.global.response.ApiResponse;
import com.raisedeveloper.server.global.security.utils.AuthUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/me/character")
	public ApiResponse<CharacterCreateResponse> createCharacter(@Valid @RequestBody CharacterCreateRequest request) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		return ApiResponse.success("CREATE_CHARACTER_SUCCESS", userService.createCharacter(userId, request));
	}
}
