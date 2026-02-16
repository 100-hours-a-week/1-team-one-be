package com.raisedeveloper.server.global.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.raisedeveloper.server.global.exception.ErrorDetail;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
	List<ErrorItem> errors
) {
	public static ErrorResponse of(String code, List<ErrorDetail> messages) {
		return new ErrorResponse(List.of(new ErrorItem(code, messages)));
	}

	public record ErrorItem(
		String code,
		List<ErrorDetail> messages
	) {
	}
}
