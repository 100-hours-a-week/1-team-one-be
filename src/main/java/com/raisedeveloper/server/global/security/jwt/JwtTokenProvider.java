package com.raisedeveloper.server.global.security.jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.common.enums.Role;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtTokenProvider {

	private final String issuer;
	private final SecretKey secretKey;
	private final long accessTtlSeconds;
	private final long refreshTtlSeconds;

	public JwtTokenProvider(@Value("${security.jwt.issuer}") String issuer,
		@Value("${security.jwt.secret-base64}") String secretBase64,
		@Value("${security.jwt.access-token-ttl-seconds}") long accessTtlSeconds,
		@Value("${security.jwt.refresh-token-ttl-seconds}") long refreshTtlSeconds) {
		this.issuer = issuer;
		this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
		this.accessTtlSeconds = accessTtlSeconds;
		this.refreshTtlSeconds = refreshTtlSeconds;
	}

	public TokenResult createAccessToken(JwtClaims claims) {
		return createToken(claims, accessTtlSeconds);
	}

	public TokenResult createRefreshToken(JwtClaims claims) {
		return createToken(claims, refreshTtlSeconds);
	}

	private TokenResult createToken(JwtClaims claims, long ttlSeconds) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(ttlSeconds);

		Map<String, Object> customClaims = new HashMap<>();
		customClaims.put(JwtClaims.CLAIM_USER_ID, claims.userId());
		customClaims.put(JwtClaims.CLAIM_EMAIL, claims.email());
		customClaims.put(JwtClaims.CLAIM_ROLE, claims.role().name());
		customClaims.put(JwtClaims.CLAIM_TOKEN_TYPE, claims.tokenType().name());
		if (claims.tokenType() == TokenType.REFRESH) {
			customClaims.put(JwtClaims.CLAIM_JTI, UUID.randomUUID().toString());
		}

		String token = Jwts.builder()
			.issuer(issuer)
			.issuedAt(Date.from(now))
			.expiration(Date.from(exp))
			.claims(customClaims)
			.signWith(secretKey, Jwts.SIG.HS256)
			.compact();

		return new TokenResult(token, LocalDateTime.ofInstant(exp, ZoneId.of("Asia/Seoul")));
	}


	public boolean validateAccessToken(String token) {
		Jws<Claims> jws = parseJws(token, TokenType.ACCESS);
		validateTokenType(jws.getPayload(), TokenType.ACCESS, ErrorCode.ACCESS_TOKEN_INVALID);
		return true;
	}

	public JwtClaims extractClaims(String token, TokenType expectedTokenType) {
		ErrorCode invalidCode = expectedTokenType == TokenType.REFRESH
			? ErrorCode.REFRESH_TOKEN_INVALID
			: ErrorCode.ACCESS_TOKEN_INVALID;

		Claims claims = parseJws(token, expectedTokenType).getPayload();
		validateTokenType(claims, expectedTokenType, invalidCode);

		Long uid = asLong(claims.get(JwtClaims.CLAIM_USER_ID));
		String email = claims.get(JwtClaims.CLAIM_EMAIL, String.class);

		Role role = parseEnum(Role.class, claims.get(JwtClaims.CLAIM_ROLE), invalidCode);
		TokenType tokenType = parseEnum(TokenType.class, claims.get(JwtClaims.CLAIM_TOKEN_TYPE), invalidCode);

		return new JwtClaims(uid, email, role, tokenType);
	}

	public Authentication getAuthentication(String token) {
		JwtClaims claims = extractClaims(token, TokenType.ACCESS);

		var authority = new SimpleGrantedAuthority(claims.role().toAuthority());

		var auth = new UsernamePasswordAuthenticationToken(claims.email(), null, java.util.List.of(authority));
		auth.setDetails(Map.of("userId", claims.userId(), "tokenType", claims.tokenType()));
		return auth;
	}

	private Jws<Claims> parseJws(String token, TokenType expectedTokenType) {
		ErrorCode expiredCode = expectedTokenType == TokenType.REFRESH
			? ErrorCode.REFRESH_TOKEN_EXPIRED
			: ErrorCode.ACCESS_TOKEN_EXPIRED;
		ErrorCode invalidCode = expectedTokenType == TokenType.REFRESH
			? ErrorCode.REFRESH_TOKEN_INVALID
			: ErrorCode.ACCESS_TOKEN_INVALID;

		try {
			return Jwts.parser().verifyWith(secretKey).requireIssuer(issuer).build().parseSignedClaims(token);
		} catch (ExpiredJwtException e) {
			throw new CustomException(expiredCode);
		} catch (UnsupportedJwtException | MalformedJwtException | SecurityException
				 | SignatureException | IllegalArgumentException e) {
			throw new CustomException(invalidCode);
		}
	}

	private static Long asLong(Object value) {
		return switch (value) {
			case null -> null;
			case Long l -> l;
			case Integer i -> i.longValue();
			case String s -> Long.parseLong(s);
			default -> throw new IllegalArgumentException("Cannot convert to Long: " + value);
		};
	}

	private static <E extends Enum<E>> E parseEnum(Class<E> enumType, Object raw, ErrorCode errorCode) {
		if (raw == null) {
			throw new CustomException(errorCode);
		}
		String string = String.valueOf(raw).trim();
		if (string.isEmpty()) {
			throw new CustomException(errorCode);
		}
		try {
			return Enum.valueOf(enumType, string);
		} catch (IllegalArgumentException e) {
			throw new CustomException(errorCode);
		}
	}

	private static void validateTokenType(Claims claims, TokenType expected, ErrorCode invalidCode) {
		Object raw = claims.get(JwtClaims.CLAIM_TOKEN_TYPE);
		TokenType tokenType = parseEnum(TokenType.class, raw, invalidCode);
		if (tokenType != expected) {
			throw new CustomException(invalidCode);
		}
	}
}
