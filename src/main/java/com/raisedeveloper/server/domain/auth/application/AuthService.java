package com.raisedeveloper.server.domain.auth.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.auth.dto.AuthSignUpRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthSignUpResponse;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserProfile;
import com.raisedeveloper.server.domain.user.infra.UserProfileRepository;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public AuthSignUpResponse signup(AuthSignUpRequest request) {
		validateEmail(request.email());
		validateNickname(request.nickname());

		String encodedPassword = passwordEncoder.encode(request.password());
		User user = new User(request.email(), encodedPassword);
		UserProfile profile = new UserProfile(user, request.nickname(), request.imagePath());

		User userSaved = userRepository.save(user);
		userProfileRepository.save(profile);
		return AuthSignUpResponse.from(userSaved);
	}

	private void validateEmail(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new CustomException(ErrorCode.USER_EMAIL_DUPLICATED);
		}
	}

	private void validateNickname(String nickname) {
		if (userProfileRepository.existsByNickname(nickname)) {
			throw new CustomException(ErrorCode.USER_NICKNAME_DUPLICATED);
		}
	}
}
