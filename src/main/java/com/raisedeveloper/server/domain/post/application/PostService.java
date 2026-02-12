package com.raisedeveloper.server.domain.post.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.post.domain.Post;
import com.raisedeveloper.server.domain.post.domain.PostImage;
import com.raisedeveloper.server.domain.post.domain.PostTag;
import com.raisedeveloper.server.domain.post.domain.Tag;
import com.raisedeveloper.server.domain.post.dto.PostCreateRequest;
import com.raisedeveloper.server.domain.post.dto.PostCreateResponse;
import com.raisedeveloper.server.domain.post.dto.PostAuthor;
import com.raisedeveloper.server.domain.post.dto.PostDetail;
import com.raisedeveloper.server.domain.post.dto.PostDetailResponse;
import com.raisedeveloper.server.domain.post.dto.PostTagInfo;
import com.raisedeveloper.server.domain.post.dto.PostUpdateRequest;
import com.raisedeveloper.server.domain.post.infra.PostImageRepository;
import com.raisedeveloper.server.domain.post.infra.PostRepository;
import com.raisedeveloper.server.domain.post.infra.PostTagRepository;
import com.raisedeveloper.server.domain.post.infra.TagRepository;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.domain.UserProfile;
import com.raisedeveloper.server.domain.user.infra.UserCharacterRepository;
import com.raisedeveloper.server.domain.user.infra.UserProfileRepository;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;
	private final UserCharacterRepository userCharacterRepository;
	private final PostRepository postRepository;
	private final PostImageRepository postImageRepository;
	private final TagRepository tagRepository;
	private final PostTagRepository postTagRepository;

	@Transactional
	public PostCreateResponse createPost(Long userId, PostCreateRequest request) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		List<String> imagePaths = normalizeList(request.images());

		String thumbnailImagePath = imagePaths.isEmpty() ? null : imagePaths.getFirst();
		Post post = postRepository.save(new Post(
			user,
			request.title(),
			request.content(),
			thumbnailImagePath
		));

		if (!imagePaths.isEmpty()) {
			List<PostImage> images = new ArrayList<>(imagePaths.size());
			short sortOrder = 1;
			for (String imagePath : imagePaths) {
				images.add(new PostImage(post, imagePath, sortOrder));
				sortOrder += 1;
			}
			postImageRepository.saveAll(images);
		}

		List<String> tagNames = normalizeList(request.tags());
		if (!tagNames.isEmpty()) {
			List<String> distinctTagNames = new ArrayList<>(new LinkedHashSet<>(tagNames));
			List<Tag> existingTags = tagRepository.findByNameIn(distinctTagNames);
			Map<String, Tag> tagByName = existingTags.stream()
				.collect(Collectors.toMap(Tag::getName, tag -> tag));

			List<Tag> missingTags = distinctTagNames.stream()
				.filter(name -> !tagByName.containsKey(name))
				.map(Tag::new)
				.toList();
			if (!missingTags.isEmpty()) {
				List<Tag> savedTags = tagRepository.saveAll(missingTags);
				savedTags.forEach(tag -> tagByName.put(tag.getName(), tag));
			}

			List<PostTag> postTags = distinctTagNames.stream()
				.map(tagByName::get)
				.filter(Objects::nonNull)
				.map(tag -> new PostTag(post, tag))
				.toList();
			if (!postTags.isEmpty()) {
				postTagRepository.saveAll(postTags);
			}
		}

		return new PostCreateResponse(post.getId());
	}

	@Transactional
	public void updatePost(Long userId, Long postId, PostUpdateRequest request) {
		Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
		validateAuthor(post, userId);

		List<String> imagePaths = normalizeList(request.images());
		String thumbnailImagePath = imagePaths.isEmpty() ? null : imagePaths.getFirst();
		post.update(request.title(), request.content(), thumbnailImagePath);

		updatePostImagesIfChanged(post, imagePaths);
		updatePostTagsIfChanged(post, request.tags());
	}

	@Transactional
	public void deletePost(Long userId, Long postId) {
		Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
		validateAuthor(post, userId);

		post.softDelete(LocalDateTime.now());
		postImageRepository.deleteAllByPostId(post.getId());
		postTagRepository.deleteAllByPostId(post.getId());
	}

	public PostDetailResponse getPostDetail(Long postId, Long viewerUserId) {
		Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		User author = post.getUser();
		UserProfile profile = userProfileRepository.findByUserId(author.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		UserCharacter character = userCharacterRepository.findByUserId(author.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.CHARACTER_NOT_SET));

		List<String> images = postImageRepository.findByPostIdOrderBySortOrderAsc(post.getId())
			.stream()
			.map(PostImage::getImagePath)
			.toList();

		List<PostTagInfo> tags = postTagRepository.findTagsByPostId(post.getId())
			.stream()
			.map(tag -> new PostTagInfo(tag.getId(), tag.getName()))
			.toList();

		boolean isAuthor = viewerUserId != null && viewerUserId.equals(author.getId());

		PostAuthor postAuthor = PostAuthor.from(author.getId(), profile, character);
		PostDetail detail = PostDetail.from(post, isAuthor, postAuthor, images, tags);

		return new PostDetailResponse(detail);
	}

	private List<String> normalizeList(List<String> values) {
		if (values == null || values.isEmpty()) {
			return List.of();
		}
		return values.stream()
			.filter(Objects::nonNull)
			.map(String::trim)
			.filter(value -> !value.isBlank())
			.toList();
	}

	private void validateAuthor(Post post, Long userId) {
		if (!post.getUser().getId().equals(userId)) {
			throw new CustomException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void replacePostImages(Post post, List<String> imagePaths) {
		postImageRepository.deleteAllByPostId(post.getId());
		if (imagePaths.isEmpty()) {
			return;
		}

		List<PostImage> images = new ArrayList<>(imagePaths.size());
		short sortOrder = 1;
		for (String imagePath : imagePaths) {
			images.add(new PostImage(post, imagePath, sortOrder));
			sortOrder += 1;
		}
		postImageRepository.saveAll(images);
	}

	private void updatePostImagesIfChanged(Post post, List<String> imagePaths) {
		List<String> existingImagePaths = postImageRepository
			.findByPostIdOrderBySortOrderAsc(post.getId())
			.stream()
			.map(PostImage::getImagePath)
			.toList();
		if (existingImagePaths.equals(imagePaths)) {
			return;
		}
		replacePostImages(post, imagePaths);
	}

	private void replacePostTags(Post post, List<String> tagNamesRaw) {
		List<String> tagNames = normalizeDistinctList(tagNamesRaw);
		postTagRepository.deleteAllByPostId(post.getId());
		if (tagNames.isEmpty()) {
			return;
		}

		List<Tag> existingTags = tagRepository.findByNameIn(tagNames);
		Map<String, Tag> tagByName = existingTags.stream()
			.collect(Collectors.toMap(Tag::getName, tag -> tag));

		List<Tag> missingTags = tagNames.stream()
			.filter(name -> !tagByName.containsKey(name))
			.map(Tag::new)
			.toList();
		if (!missingTags.isEmpty()) {
			List<Tag> savedTags = tagRepository.saveAll(missingTags);
			savedTags.forEach(tag -> tagByName.put(tag.getName(), tag));
		}

		List<PostTag> postTags = tagNames.stream()
			.map(tagByName::get)
			.filter(Objects::nonNull)
			.map(tag -> new PostTag(post, tag))
			.toList();
		if (!postTags.isEmpty()) {
			postTagRepository.saveAll(postTags);
		}
	}

	private void updatePostTagsIfChanged(Post post, List<String> tagNamesRaw) {
		List<String> tagNames = normalizeDistinctList(tagNamesRaw);
		Set<String> existingTagNames = Set.copyOf(postTagRepository.findTagNamesByPostId(post.getId()));
		if (existingTagNames.equals(Set.copyOf(tagNames))) {
			return;
		}
		replacePostTags(post, tagNames);
	}

	private List<String> normalizeDistinctList(List<String> values) {
		return new ArrayList<>(new LinkedHashSet<>(normalizeList(values)));
	}
}
