package com.raisedeveloper.server.global.security.jwt;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
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

		jwtTokenProvider.validateAccessToken(token);

		var authentication = jwtTokenProvider.getAuthentication(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);

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
