package com.raisedeveloper.server.domain.image.application;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.raisedeveloper.server.domain.image.dto.PresignedUrlRequest;
import com.raisedeveloper.server.domain.image.dto.PresignedUrlResponse;
import com.raisedeveloper.server.domain.image.enums.ImageType;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

	private final StorageService storageService;

	@Value("#{'${image.allowed-extensions.profile}'.split(',')}")
	private List<String> profileImageExtensions;

	@Value("#{'${image.allowed-extensions.post}'.split(',')}")
	private List<String> postImageExtensions;

	@Value("${image.presigned-url-duration}")
	private int presignedUrlDuration;

	public PresignedUrlResponse generateUploadUrl(
		ImageType imageType,
		PresignedUrlRequest request
	) {
		String extension = extractExtension(request.fileName());
		validateExtension(imageType, extension);

		String uniqueFileName = generateUniqueFileName(extension);

		String filePath = imageType.getBasePath() + uniqueFileName;

		String presignedUrl = storageService.generatePresignedUrl(
			filePath,
			request.contentType(),
			Duration.ofMinutes(presignedUrlDuration)
		);
		
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(presignedUrlDuration);

		log.info("Generated presigned URL for imageType: {}, filePath: {}",
			imageType, filePath);

		return PresignedUrlResponse.of(presignedUrl, filePath, expiresAt);
	}

	public void deleteImage(String filePath) {
		validateFilePath(filePath);

		storageService.deleteFile(filePath);

		log.info("Deleted image: {}", filePath);
	}

	private String extractExtension(String fileName) {
		int lastDotIndex = fileName.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
			throw new CustomException(
				ErrorCode.INVALID_FILE_EXTENSION,
				List.of(ErrorDetail.field("fileName", fileName))
			);
		}
		return fileName.substring(lastDotIndex + 1).toLowerCase();
	}

	private void validateExtension(ImageType imageType, String extension) {
		List<String> allowedExtensions = imageType == ImageType.USER_PROFILE
			? profileImageExtensions
			: postImageExtensions;
		if (!allowedExtensions.contains(extension)) {
			throw new CustomException(
				ErrorCode.INVALID_FILE_EXTENSION,
				List.of(ErrorDetail.field("extension", extension))
			);
		}
	}

	private String generateUniqueFileName(String extension) {
		String uuid = UUID.randomUUID().toString();
		return uuid + "." + extension;
	}

	private void validateFilePath(String filePath) {
		if (filePath == null || filePath.isBlank()) {
			throw new CustomException(
				ErrorCode.INVALID_FILE_PATH,
				List.of(ErrorDetail.field("filePath", filePath))
			);
		}

		// 경로 traversal 공격 방지
		if (filePath.contains("..") || filePath.contains("//")) {
			throw new CustomException(
				ErrorCode.INVALID_FILE_PATH,
				List.of(ErrorDetail.field("filePath", filePath))
			);
		}
	}
}
