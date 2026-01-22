package com.raisedeveloper.server.global.pagination;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;

@Component
public class CursorTokenCodec {

	private static final String HMAC_ALGORITHM = "HmacSHA256";

	private final SecretKeySpec keySpec;

	public CursorTokenCodec(@Value("${TOKEN_HASH_SECRET}") String secret) {
		this.keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
	}

	public String encode(String payload) {
		String payloadEncoded = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));
		String signature = base64UrlEncode(hmac(payload));
		return payloadEncoded + "." + signature;
	}

	public String decode(String token) {
		if (token == null || token.isBlank()) {
			return null;
		}
		String[] parts = token.split("\\.");
		if (parts.length != 2) {
			throw invalidCursor();
		}
		String payloadEncoded = parts[0];
		String signature = parts[1];
		String payload = new String(base64UrlDecode(payloadEncoded), StandardCharsets.UTF_8);
		String expectedSignature = base64UrlEncode(hmac(payload));
		if (!expectedSignature.equals(signature)) {
			throw invalidCursor();
		}
		return payload;
	}

	private byte[] hmac(String payload) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(keySpec);
			return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private String base64UrlEncode(byte[] value) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
	}

	private byte[] base64UrlDecode(String value) {
		return Base64.getUrlDecoder().decode(value);
	}

	private CustomException invalidCursor() {
		return new CustomException(
			ErrorCode.VALIDATION_FAILED,
			java.util.List.of(ErrorDetail.field("cursor", "invalid cursor"))
		);
	}
}
