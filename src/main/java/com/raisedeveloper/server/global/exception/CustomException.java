package com.raisedeveloper.server.global.exception;

import java.util.List;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

	private final ErrorCode errorCode;
	private final List<ErrorDetail> errors;

	public CustomException(ErrorCode errorCode) {
		super(errorCode.getCode());
		this.errorCode = errorCode;
		this.errors = List.of(ErrorDetail.reasonOnly(errorCode.getReason()));
	}

	public CustomException(ErrorCode errorCode, List<ErrorDetail> errors) {
		super(errorCode.getCode());
		this.errorCode = errorCode;
		this.errors = errors;
	}
}
