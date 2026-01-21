package com.raisedeveloper.server.global.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.raisedeveloper.server.global.response.ApiResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		List<ErrorDetail> errors = ex.getErrors();

		return ResponseEntity.status(errorCode.getHttpStatusCode())
			.body(ApiResponse.fail(errorCode.getCode(), errors));
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ApiResponse<Void>> handleBindException(BindException ex) {
		ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
		List<ErrorDetail> errors = mapFieldErrors(ex.getBindingResult());
		return ResponseEntity.status(errorCode.getHttpStatusCode())
			.body(ApiResponse.fail(errorCode.getCode(), errors));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
		ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
		List<ErrorDetail> errors = ex.getConstraintViolations().stream()
			.map(this::mapConstraintViolation)
			.toList();
		return ResponseEntity.status(errorCode.getHttpStatusCode())
			.body(ApiResponse.fail(errorCode.getCode(), errors));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		log.error(ex.getMessage(), ex);
		return ResponseEntity.status(errorCode.getHttpStatusCode())
			.body(ApiResponse.fail(
				errorCode.getCode(),
				List.of(new ErrorDetail(null, errorCode.getCode()))
			));
	}

	private List<ErrorDetail> mapFieldErrors(BindingResult bindingResult) {
		List<ErrorDetail> errors = bindingResult.getFieldErrors().stream()
			.map(this::mapFieldError)
			.toList();
		if (!errors.isEmpty()) {
			return errors;
		}
		return List.of(ErrorDetail.reasonOnly(ErrorCode.VALIDATION_FAILED.getReason()));
	}

	private ErrorDetail mapFieldError(FieldError error) {
		return ErrorDetail.field(error.getField(), error.getDefaultMessage());
	}

	private ErrorDetail mapConstraintViolation(ConstraintViolation<?> violation) {
		String path = violation.getPropertyPath() == null ? null : violation.getPropertyPath().toString();
		return ErrorDetail.field(extractFieldName(path), violation.getMessage());
	}

	private String extractFieldName(String path) {
		if (path == null || path.isBlank()) {
			return null;
		}
		int lastDot = path.lastIndexOf('.');
		return lastDot == -1 ? path : path.substring(lastDot + 1);
	}
}
