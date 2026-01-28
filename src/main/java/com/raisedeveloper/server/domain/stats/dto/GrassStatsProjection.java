package com.raisedeveloper.server.domain.stats.dto;

import java.time.LocalDate;

public interface GrassStatsProjection {
	LocalDate getDate();

	Long getTargetCount();

	Long getSuccessCount();
}
