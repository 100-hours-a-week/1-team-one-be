package com.raisedeveloper.server.domain.user.dto;

import com.raisedeveloper.server.domain.user.enums.CharacterType;

import jakarta.validation.constraints.NotNull;

public record CharacterCreateRequest(
	@NotNull
	CharacterType type
) {
}
