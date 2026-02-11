package com.raisedeveloper.server.domain.user.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import com.raisedeveloper.server.domain.user.enums.CharacterType;

import jakarta.validation.constraints.NotNull;

public record CharacterCreateRequest(
	@NotNull(message = USER_CHARACTER_TYPE_REQUIRED)
	CharacterType type
) {
}
