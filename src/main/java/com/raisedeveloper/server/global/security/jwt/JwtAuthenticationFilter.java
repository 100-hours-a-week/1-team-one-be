package com.raisedeveloper.server.global.security.jwt;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.raisedeveloper.server.global.exception.CustomException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final RequestMatcher optionalAuthMatcher;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, RequestMatcher optionalAuthMatcher) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.optionalAuthMatcher = optionalAuthMatcher;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String token = resolveBearerToken(request);

		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			jwtTokenProvider.validateAccessToken(token);

			var authentication = jwtTokenProvider.getAuthentication(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (CustomException ex) {
			if (ex.getErrorCode() != null && ex.getErrorCode().isJwtError() && optionalAuthMatcher.matches(request)) {
				SecurityContextHolder.clearContext();
				filterChain.doFilter(request, response);
				return;
			}
			throw ex;
		}

		filterChain.doFilter(request, response);
	}

	private String resolveBearerToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (!StringUtils.hasText(header)) {
			return null;
		}

		String prefix = "Bearer ";
		if (!header.startsWith(prefix)) {
			return null;
		}

		String token = header.substring(prefix.length()).trim();
		return StringUtils.hasText(token) ? token : null;
	}
}
