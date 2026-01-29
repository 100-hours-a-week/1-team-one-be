package com.raisedeveloper.server.domain.image.infra;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.raisedeveloper.server.domain.image.application.StorageService;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.provider", havingValue = "gcs")
public class GcsStorageService implements StorageService {

	private final Storage storage;

	@Value("${storage.gcs.bucket-name}")
	private String bucketName;

	@Override
	public String generatePresignedUrl(String filePath, String contentType, Duration duration) {
		try {
			BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, filePath))
				.setContentType(contentType)
				.build();

			URL url = storage.signUrl(
				blobInfo,
				duration.toMinutes(),
				TimeUnit.MINUTES,
				Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
				Storage.SignUrlOption.withV4Signature()
			);

			return url.toString();
		} catch (Exception e) {
			log.error("Failed to generate presigned URL for path: {}", filePath, e);
			throw new CustomException(
				ErrorCode.PRESIGNED_URL_GENERATION_FAILED,
				List.of(ErrorDetail.field("filePath", filePath))
			);
		}
	}

	@Override
	public void deleteFile(String filePath) {
		try {
			BlobId blobId = BlobId.of(bucketName, filePath);
			boolean deleted = storage.delete(blobId);

			if (!deleted) {
				throw new CustomException(
					ErrorCode.IMAGE_NOT_FOUND,
					List.of(ErrorDetail.field("filePath", filePath))
				);
			}

			log.info("Successfully deleted file: {}", filePath);
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to delete file: {}", filePath, e);
			throw new CustomException(
				ErrorCode.IMAGE_DELETE_FAILED,
				List.of(ErrorDetail.field("filePath", filePath))
			);
		}
	}
}
