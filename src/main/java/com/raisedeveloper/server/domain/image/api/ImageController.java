package com.raisedeveloper.server.domain.image.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.image.application.ImageService;
import com.raisedeveloper.server.domain.image.dto.PresignedUrlRequest;
import com.raisedeveloper.server.domain.image.dto.PresignedUrlResponse;
import com.raisedeveloper.server.domain.image.enums.ImageType;
import com.raisedeveloper.server.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

	private final ImageService imageService;

	@PostMapping("/upload-url/profile")
	public ApiResponse<PresignedUrlResponse> generateProfileImageUploadUrl(
		@Valid @RequestBody PresignedUrlRequest request
	) {
		PresignedUrlResponse response = imageService.generateUploadUrl(
			ImageType.USER_PROFILE,
			request
		);
		return ApiResponse.of("PRESIGNED_URL_GENERATED", response);
	}

	@PostMapping("/upload-url/post")
	public ApiResponse<PresignedUrlResponse> generatePostImageUploadUrl(
		@Valid @RequestBody PresignedUrlRequest request
	) {
		PresignedUrlResponse response = imageService.generateUploadUrl(
			ImageType.POST_IMAGE,
			request
		);
		return ApiResponse.of("PRESIGNED_URL_GENERATED", response);
	}
}
