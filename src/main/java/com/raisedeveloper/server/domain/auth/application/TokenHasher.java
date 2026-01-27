package com.raisedeveloper.server.domain.auth.application;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenHasher {
	private final SecretKeySpec keySpec;

	public TokenHasher(@Value("${TOKEN_HASH_SECRET}") String secret) {
		this.keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

	public String hmacSha256Base64Url(String token) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(keySpec);
			byte[] digest = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(digest); // 인덱스용으로 짧고 안전
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}

