package com.raisedeveloper.server.domain.image.infra;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.raisedeveloper.server.domain.image.application.StorageService;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3StorageService implements StorageService {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;

	@Value("${storage.s3.bucket-name}")
	private String bucketName;

	@Override
	public String generatePresignedUrl(String filePath, String contentType, Duration duration) {
		try {
			PutObjectRequest.Builder putObjectBuilder = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(filePath);

			if (contentType != null && !contentType.isBlank()) {
				putObjectBuilder.contentType(contentType);
			}

			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(duration)
				.putObjectRequest(putObjectBuilder.build())
				.build();

			PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
			return presignedRequest.url().toString();
		} catch (Exception e) {
			log.error("Failed to generate S3 presigned URL for path: {}", filePath, e);
			throw new CustomException(ErrorCode.PRESIGNED_URL_GENERATION_FAILED);
		}
	}

	@Override
	public void deleteFile(String filePath) {
		try {
			s3Client.headObject(
				HeadObjectRequest.builder()
					.bucket(bucketName)
					.key(filePath)
					.build()
			);

			s3Client.deleteObject(
				DeleteObjectRequest.builder()
					.bucket(bucketName)
					.key(filePath)
					.build()
			);

			log.info("Successfully deleted S3 object: {}", filePath);
		} catch (Exception e) {
			log.error("Failed to delete S3 object: {}", filePath, e);
			throw new CustomException(
				ErrorCode.IMAGE_DELETE_FAILED,
				List.of(ErrorDetail.field("filePath", filePath))
			);
		}
	}
}
