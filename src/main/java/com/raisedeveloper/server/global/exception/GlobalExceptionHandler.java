package com.raisedeveloper.server.global.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.raisedeveloper.server.global.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		List<ErrorDetail> errors = ex.getErrors();

		return ResponseEntity.status(errorCode.getHttpStatusCode())
			.body(ApiResponse.fail(errorCode.getCode(), errors));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		return ResponseEntity.status(errorCode.getHttpStatusCode())
			.body(ApiResponse.fail(
				errorCode.getCode(),
				List.of(new ErrorDetail(null, errorCode.getCode()))
			));
	}
}

