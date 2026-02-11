package com.raisedeveloper.server.global.exception;

public final class ErrorMessageConstants {

	private ErrorMessageConstants() {
	}

	// Post Domain
	public static final String POST_IMAGES_TOO_MANY_MESSAGE =
		"하나의 게시글에 이미지는 최대 10개까지 포함할 수 있습니다.";
	public static final String POST_TITLE_BLANK_MESSAGE = "게시글 제목은 비어 있을 수 없습니다.";
	public static final String POST_TITLE_TOO_LONG_MESSAGE = "게시글 제목은 최대 50자까지 가능합니다.";
	public static final String POST_CONTENT_BLANK_MESSAGE = "게시글 내용은 비어 있을 수 없습니다.";
	public static final String POST_CONTENT_TOO_LONG_MESSAGE = "게시글 내용은 최대 500자까지 가능합니다.";
	public static final String POST_IMAGE_PATH_BLANK_MESSAGE = "이미지 경로는 비어 있을 수 없습니다.";
	public static final String POST_TAGS_TOO_MANY_MESSAGE = "태그는 최대 5개까지 가능합니다.";
	public static final String POST_TAG_NAME_BLANK_MESSAGE = "태그 이름은 비어 있을 수 없습니다.";
	public static final String POST_TAG_NAME_TOO_LONG_MESSAGE = "태그 이름은 최대 10자까지 가능합니다.";
	public static final String POST_NOT_FOUND_MESSAGE = "해당 게시글을 찾을 수 없습니다.";
}
