package com.raisedeveloper.server.domain.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.infra.UserCharacterRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserCharacterService {

	private final UserCharacterRepository userCharacterRepository;

	public UserCharacter getByUserIdOrThrow(Long userId) {
		return userCharacterRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHARACTER_NOT_SET));
	}
}
