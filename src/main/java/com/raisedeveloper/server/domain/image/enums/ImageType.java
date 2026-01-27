package com.raisedeveloper.server.domain.image.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageType {

	USER_PROFILE("users/profile/"),
	POST_IMAGE("posts/images/");

	private final String basePath;
}
