package com.raisedeveloper.server.domain.image.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.image.dto.PresignedUrlResponse;

@Component
public class ImageMapper {

	public PresignedUrlResponse toPresignedUrlResponse(String uploadUrl, String filePath, LocalDateTime expiresAt) {
		return new PresignedUrlResponse(uploadUrl, filePath, expiresAt);
	}
}
