package com.raisedeveloper.server.domain.image.application;

import java.time.Duration;

public interface StorageService {

	String generatePresignedUrl(String filePath, String contentType, Duration duration);

	void deleteFile(String filePath);
}
