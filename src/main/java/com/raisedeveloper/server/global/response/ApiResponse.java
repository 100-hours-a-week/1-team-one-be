package com.raisedeveloper.server.global.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.raisedeveloper.server.global.exception.ErrorDetail;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
	String code,
	T data,
	List<ErrorDetail> errors
) {
	public static <T> ApiResponse<T> success(String code, T data) {
		return new ApiResponse<>(code, data, null);
	}

	public static <T> ApiResponse<T> fail(String code, List<ErrorDetail> errors) {
		return new ApiResponse<>(code, null, errors);
	}
}

