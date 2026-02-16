package com.raisedeveloper.server.domain.post.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.post.application.PostService;
import com.raisedeveloper.server.domain.post.dto.PostCreateRequest;
import com.raisedeveloper.server.domain.post.dto.PostCreateResponse;
import com.raisedeveloper.server.domain.post.dto.PostUpdateRequest;
import com.raisedeveloper.server.global.response.ApiResponse;
import com.raisedeveloper.server.global.security.utils.AuthUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<PostCreateResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		PostCreateResponse res = postService.createPost(userId, request);
		return ApiResponse.of("CREATE_POST_SUCCESS", res);
	}

	@PutMapping("/{postId}")
	public ApiResponse<Object> updatePost(
		@PathVariable Long postId,
		@Valid @RequestBody PostUpdateRequest request
	) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		postService.updatePost(userId, postId, request);
		return ApiResponse.of("UPDATE_POST_SUCCESS", Map.of());
	}

	@DeleteMapping("/{postId}")
	public ApiResponse<Object> deletePost(@PathVariable Long postId) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		postService.deletePost(userId, postId);
		return ApiResponse.of("DELETE_POST_SUCCESS", Map.of());
	}
}
