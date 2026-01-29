package com.raisedeveloper.server.domain.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonConverter {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static JsonNode from(String json) {
		JsonNode jsonNode;
		try {
			jsonNode = objectMapper.readTree(json);
		} catch (Exception e) {
			jsonNode = objectMapper.createObjectNode();
		}
		return jsonNode;
	}
}
