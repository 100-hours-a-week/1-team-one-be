package com.raisedeveloper.server.domain.post.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.post.application.PostService;
import com.raisedeveloper.server.domain.post.dto.PostCreateRequest;
import com.raisedeveloper.server.domain.post.dto.PostCreateResponse;
import com.raisedeveloper.server.domain.post.dto.PostDetailResponse;
import com.raisedeveloper.server.domain.post.dto.PostLikeRequest;
import com.raisedeveloper.server.domain.post.dto.PostLikeResponse;
import com.raisedeveloper.server.domain.post.dto.PostListResponse;
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

	@GetMapping
	public ApiResponse<PostListResponse> getPosts(
		@RequestParam(value = "limit", required = false) Integer limit,
		@RequestParam(value = "cursor", required = false) String cursor,
		@RequestParam(value = "author-id", required = false) Long authorId,
		@RequestParam(value = "tag", required = false) List<String> tagNames
	) {
		Long userId = AuthUtils.resolveUserIdFromContextOrNull();
		PostListResponse res = postService.getPosts(authorId, tagNames, limit, cursor, userId);
		return ApiResponse.of("GET_POSTS_SUCCESS", res);
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

	@GetMapping("/{postId}")
	public ApiResponse<PostDetailResponse> getPostDetail(@PathVariable Long postId) {
		Long userId = AuthUtils.resolveUserIdFromContextOrNull();
		PostDetailResponse response = postService.getPostDetail(postId, userId);
		return ApiResponse.of("GET_POST_DETAIL_SUCCESS", response);
	}

	@PostMapping("/{postId}/like")
	public ApiResponse<PostLikeResponse> togglePostLike(
		@PathVariable Long postId,
		@Valid @RequestBody PostLikeRequest request
	) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		PostLikeResponse response = postService.togglePostLike(userId, postId, request.liked());
		return ApiResponse.of("TOGGLE_POST_LIKE_SUCCESS", response);
	}
}
