package com.raisedeveloper.server.global.security.utils;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.security.jwt.TokenType;

public class AuthUtils {

	public static Long resolveUserIdFromContext() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new CustomException(ErrorCode.MISSING_TOKEN);
		}
		Object details = authentication.getDetails();
		if (!(details instanceof Map<?, ?> detailsMap)) {
			throw new CustomException(ErrorCode.ACCESS_TOKEN_INVALID);
		}
		Object tokenType = detailsMap.get("tokenType");
		if (tokenType != TokenType.ACCESS) {
			throw new CustomException(ErrorCode.ACCESS_TOKEN_INVALID);
		}
		Object userId = detailsMap.get("userId");
		if (userId instanceof Long id) {
			return id;
		}
		throw new CustomException(ErrorCode.ACCESS_TOKEN_INVALID);
	}

	public static Long resolveUserIdFromContextOrNull() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		Object details = authentication.getDetails();
		if (!(details instanceof Map<?, ?> detailsMap)) {
			return null;
		}
		Object tokenType = detailsMap.get("tokenType");
		if (tokenType != TokenType.ACCESS) {
			return null;
		}
		Object userId = detailsMap.get("userId");
		if (userId instanceof Long id) {
			return id;
		}
		return null;
	}
}
