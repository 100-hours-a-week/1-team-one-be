package com.raisedeveloper.server.domain.auth.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.auth.domain.RefreshToken;
import com.raisedeveloper.server.domain.auth.domain.FcmToken;
import com.raisedeveloper.server.domain.auth.infra.FcmTokenRepository;
import com.raisedeveloper.server.domain.auth.dto.AuthLoginRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthLoginResponse;
import com.raisedeveloper.server.domain.auth.dto.AuthRefreshRequest;
import com.raisedeveloper.server.domain.auth.dto.AuthRefreshResponse;
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
import com.raisedeveloper.server.global.exception.ErrorDetail;
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
	private final FcmTokenRepository fcmTokenRepository;
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

	@Transactional
	public AuthRefreshResponse refresh(AuthRefreshRequest request) {
		jwtTokenProvider.validate(request.refreshToken());

		JwtClaims jwtClaims = jwtTokenProvider.extractClaims(request.refreshToken());
		if (jwtClaims.tokenType() != TokenType.REFRESH) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}

		String tokenHash = tokenHasher.hmacSha256Base64Url(request.refreshToken());
		RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash).orElseThrow(
			() -> new CustomException(ErrorCode.INVALID_TOKEN));
		validateRefreshTokenState(refreshToken);
		refreshToken.revoke();

		User user = userRepository.findByEmailAndDeletedAtIsNull(jwtClaims.email()).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)
		);
		Tokens tokens = issueTokens(user);
		TokenResult newRefreshToken = tokens.refreshToken();
		String newTokenHash = tokenHasher.hmacSha256Base64Url(newRefreshToken.token());
		refreshTokenRepository.save(new RefreshToken(user, newTokenHash, newRefreshToken.expiresAt()));

		return new AuthRefreshResponse(tokens);
	}

	@Transactional
	public void logoutAll(Long userId) {
		List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId);
		for (RefreshToken token : tokens) {
			if (token.getRevokedAt() == null) {
				token.revoke();
			}
		}
	}

	@Transactional
	public void setFcmToken(Long userId, String fcmToken) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)
		);

		fcmTokenRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
			.ifPresentOrElse(
				existing -> existing.updateToken(fcmToken),
				() -> fcmTokenRepository.save(new FcmToken(user, fcmToken))
			);
	}

	public boolean isEmailAvailable(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new CustomException(
				ErrorCode.USER_EMAIL_DUPLICATED,
				List.of(ErrorDetail.field("email", "email already in use"))
			);
		}
		return true;
	}

	public boolean isNicknameAvailable(String nickname) {
		if (userProfileRepository.existsByNickname(nickname)) {
			throw new CustomException(
				ErrorCode.USER_NICKNAME_DUPLICATED,
				List.of(ErrorDetail.field("nickname", "nickname already in use"))
			);
		}
		return true;
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

	private void validateRefreshTokenState(RefreshToken refreshToken) {
		if (refreshToken.getRevokedAt() != null) {
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
		if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new CustomException(ErrorCode.EXPIRED_TOKEN);
		}
	}
}
