package com.raisedeveloper.server.domain.image.dto;

import java.time.LocalDateTime;

public record PresignedUrlResponse(
	String uploadUrl,
	String filePath,
	LocalDateTime expiresAt
) {
}
