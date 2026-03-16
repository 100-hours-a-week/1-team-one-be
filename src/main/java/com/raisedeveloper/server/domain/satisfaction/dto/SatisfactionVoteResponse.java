package com.raisedeveloper.server.domain.satisfaction.dto;

public record SatisfactionVoteResponse(
	Long exerciseSessionId,
	Long routineId,
	Byte satisfaction
) {
}
