package com.raisedeveloper.server.domain.userprofile.dto;

import java.util.Map;

public record AiUserProfileDto(
	Long userId,
	Map<String, Double> bodyPartRatios,
	Map<String, Double> exerciseTypeRatios,
	Map<String, Double> difficultyRatios,
	double weeklyFrequency
) {
}
