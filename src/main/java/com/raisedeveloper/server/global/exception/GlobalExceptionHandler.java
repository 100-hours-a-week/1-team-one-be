package com.raisedeveloper.server.global.exception;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
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

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex) {
		Throwable cause = getDeepestCause(ex);

		if (cause instanceof InvalidFormatException ife) {

			String field = extractFieldPath(ife.getPath());
			Class<?> targetClass = extractRawClass(ife.getTargetType());

			if (targetClass != null && targetClass.isEnum()) {
				@SuppressWarnings("unchecked")
				Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>)targetClass;

				List<ErrorDetail> errors = List.of(toEnumError(field, enumClass));
				ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
				return ResponseEntity.status(errorCode.getHttpStatusCode())
					.body(ApiResponse.fail(errorCode.getCode(), errors));
			}
		}

		ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
		return ResponseEntity.status(errorCode.getHttpStatusCode())
			.body(ApiResponse.fail(
				errorCode.getCode(),
				List.of(ErrorDetail.reasonOnly(errorCode.getReason()))
			));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
		Class<?> targetType = ex.getRequiredType();
		if (targetType != null && targetType.isEnum()) {
			@SuppressWarnings("unchecked")
			Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>)targetType;
			String field = ex.getName();
			ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
			return ResponseEntity.status(errorCode.getHttpStatusCode())
				.body(ApiResponse.fail(errorCode.getCode(), List.of(toEnumError(field, enumClass))));
		}
		ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
		return ResponseEntity.status(errorCode.getHttpStatusCode())
			.body(ApiResponse.fail(
				errorCode.getCode(),
				List.of(ErrorDetail.reasonOnly(errorCode.getReason()))
			));
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

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;
		return ResponseEntity.status(errorCode.getHttpStatusCode())
			.body(ApiResponse.fail(
				errorCode.getCode(),
				List.of(ErrorDetail.reasonOnly(errorCode.getReason()))
			));
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

	private static String extractFieldPath(List<JsonMappingException.Reference> path) {
		if (path == null || path.isEmpty()) {
			return null;
		}
		return path.stream()
			.map(ref -> ref.getFieldName() != null ? ref.getFieldName() : String.valueOf(ref.getIndex()))
			.collect(Collectors.joining("."));
	}

	private static Class<?> extractRawClass(Class<?> targetType) {
		return targetType;
	}

	private ErrorDetail toEnumError(String field, Class<? extends Enum<?>> enumClass) {
		String allowed = Arrays.stream(enumClass.getEnumConstants())
			.map(Enum::name)
			.collect(Collectors.joining(", ", "(", ")"));
		String reason = (field == null ? "value" : field) + " must be " + allowed;
		return ErrorDetail.field(field, reason);
	}

	private Throwable getDeepestCause(Throwable ex) {
		Throwable cause = ex;
		while (cause.getCause() != null && cause.getCause() != cause) {
			cause = cause.getCause();
		}
		return cause;
	}
}
