package com.raisedeveloper.server.global.pagination;

import java.time.LocalDateTime;

public record Cursor(
	LocalDateTime createdAt,
	Long id
) {
}
