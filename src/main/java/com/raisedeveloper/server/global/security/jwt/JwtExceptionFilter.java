package com.raisedeveloper.server.global.security.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;
import com.raisedeveloper.server.global.response.ErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtExceptionFilter extends OncePerRequestFilter {

	private final ObjectMapper objectMapper;

	public JwtExceptionFilter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		try {
			filterChain.doFilter(request, response);
		} catch (CustomException ex) {
			ErrorCode ec = ex.getErrorCode();
			if (ec != null && ec.isJwtError() && !response.isCommitted()) {
				writeJwtError(response, ec);
			}
		}
	}

	private void writeJwtError(HttpServletResponse response, ErrorCode ec) throws IOException {
		response.setStatus(ec.getHttpStatusCode());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		var body = ErrorResponse.of(
			ec.getCode(),
			List.of(ErrorDetail.reasonOnly(ec.getReason()))
		);

		response.getWriter().write(objectMapper.writeValueAsString(body));
	}
}
