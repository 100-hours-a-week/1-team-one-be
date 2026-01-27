package com.raisedeveloper.server.domain.image.dto;

import java.time.LocalDateTime;

public record PresignedUrlResponse(
	String uploadUrl,
	String filePath,
	LocalDateTime expiresAt
) {
	public static PresignedUrlResponse of(String uploadUrl, String filePath, LocalDateTime expiresAt) {
		return new PresignedUrlResponse(uploadUrl, filePath, expiresAt);
	}
}
