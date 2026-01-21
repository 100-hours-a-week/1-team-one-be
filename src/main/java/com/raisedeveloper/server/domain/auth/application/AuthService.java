package com.raisedeveloper.server.domain.auth.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.auth.domain.RefreshToken;
import com.raisedeveloper.server.domain.auth.dto.AuthLoginRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthLoginResponse;
import com.raisedeveloper.server.domain.auth.dto.AuthSignUpRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthSignUpResponse;
import com.raisedeveloper.server.domain.auth.dto.Tokens;
import com.raisedeveloper.server.domain.auth.infra.RefreshTokenRepository;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserProfile;
import com.raisedeveloper.server.domain.user.infra.UserProfileRepository;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.security.jwt.JwtClaims;
import com.raisedeveloper.server.global.security.jwt.JwtTokenProvider;
import com.raisedeveloper.server.global.security.jwt.TokenResult;
import com.raisedeveloper.server.global.security.jwt.TokenType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserProfileRepository userProfileRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenHasher tokenHasher;
	private final JwtTokenProvider jwtTokenProvider;

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

	@Transactional
	public AuthLoginResponse login(AuthLoginRequest request) {
		User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_INVALID_CREDENTIALS));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new CustomException(ErrorCode.AUTH_INVALID_CREDENTIALS);
		}

		Tokens tokens = issueTokens(user);

		TokenResult refreshToken = tokens.refreshToken();
		String tokenHash = tokenHasher.hmacSha256Base64Url(refreshToken.token());
		refreshTokenRepository.save(new RefreshToken(user, tokenHash, refreshToken.expiresAt()));

		return AuthLoginResponse.from(tokens, user);
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

	private Tokens issueTokens(User user) {
		TokenResult accessToken = jwtTokenProvider.createAccessToken(
			new JwtClaims(user.getId(), user.getEmail(), user.getRole(), TokenType.ACCESS));
		TokenResult refreshToken = jwtTokenProvider.createRefreshToken(
			new JwtClaims(user.getId(), user.getEmail(), user.getRole(), TokenType.REFRESH));

		return new Tokens(accessToken, refreshToken);
	}
}
