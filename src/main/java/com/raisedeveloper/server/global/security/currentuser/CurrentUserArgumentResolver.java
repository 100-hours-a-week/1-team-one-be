package com.raisedeveloper.server.global.security.currentuser;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.security.jwt.TokenType;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (!parameter.hasParameterAnnotation(CurrentUser.class)) {
			return false;
		}
		Class<?> type = parameter.getParameterType();
		return type.equals(Long.class) || type.equals(CurrentUserPrincipal.class);
	}

	@Override
	public Object resolveArgument(
		MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory
	) {
		CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
		boolean required = annotation == null || annotation.required();

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			if (required) {
				throw new CustomException(ErrorCode.MISSING_TOKEN);
			}
			return null;
		}

		Object principal = authentication.getPrincipal();
		if (!(principal instanceof CurrentUserPrincipal currentUser)) {
			if (required) {
				throw new CustomException(ErrorCode.ACCESS_TOKEN_INVALID);
			}
			return null;
		}

		if (currentUser.tokenType() != TokenType.ACCESS || currentUser.userId() == null) {
			if (required) {
				throw new CustomException(ErrorCode.ACCESS_TOKEN_INVALID);
			}
			return null;
		}

		if (parameter.getParameterType().equals(CurrentUserPrincipal.class)) {
			return currentUser;
		}
		return currentUser.userId();
	}
}
