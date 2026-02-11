package com.raisedeveloper.server.domain.post.application;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.post.domain.Post;
import com.raisedeveloper.server.domain.post.domain.PostImage;
import com.raisedeveloper.server.domain.post.domain.PostTag;
import com.raisedeveloper.server.domain.post.domain.Tag;
import com.raisedeveloper.server.domain.post.dto.PostCreateRequest;
import com.raisedeveloper.server.domain.post.dto.PostCreateResponse;
import com.raisedeveloper.server.domain.post.infra.PostImageRepository;
import com.raisedeveloper.server.domain.post.infra.PostRepository;
import com.raisedeveloper.server.domain.post.infra.PostTagRepository;
import com.raisedeveloper.server.domain.post.infra.TagRepository;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	private final UserRepository userRepository;
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
}
