package com.raisedeveloper.server.domain.stats.dto;

import java.time.LocalDate;

public record GrassDataDto(
	LocalDate date,
	int targetCount,
	int successCount
) {
}
