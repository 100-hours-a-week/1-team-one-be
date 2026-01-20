package com.raisedeveloper.server.global.exception;

public record ErrorDetail(
	String field,
	String reason
) {
	public static ErrorDetail reasonOnly(String reason) {
		return new ErrorDetail(null, reason);
	}

	public static ErrorDetail field(String field, String reason) {
		return new ErrorDetail(field, reason);
	}
}
