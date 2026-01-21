package com.raisedeveloper.server.domain.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.dto.CharacterCreateRequest;
import com.raisedeveloper.server.domain.user.dto.CharacterCreateResponse;
import com.raisedeveloper.server.domain.user.infra.UserCharacterRepository;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final UserCharacterRepository userCharacterRepository;

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
