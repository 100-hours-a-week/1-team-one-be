package com.raisedeveloper.server.domain.post.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.post.domain.Post;
import com.raisedeveloper.server.domain.post.domain.PostImage;
import com.raisedeveloper.server.domain.post.domain.PostTag;
import com.raisedeveloper.server.domain.post.domain.Tag;
import com.raisedeveloper.server.domain.post.dto.PostAuthor;
import com.raisedeveloper.server.domain.post.dto.PostCreateRequest;
import com.raisedeveloper.server.domain.post.dto.PostCreateResponse;
import com.raisedeveloper.server.domain.post.dto.PostDetail;
import com.raisedeveloper.server.domain.post.dto.PostDetailResponse;
import com.raisedeveloper.server.domain.post.dto.PostLikeResponse;
import com.raisedeveloper.server.domain.post.dto.PostListItem;
import com.raisedeveloper.server.domain.post.dto.PostListResponse;
import com.raisedeveloper.server.domain.post.dto.PostMetaDetailResponse;
import com.raisedeveloper.server.domain.post.dto.PostMetaItem;
import com.raisedeveloper.server.domain.post.dto.PostMetaListResponse;
import com.raisedeveloper.server.domain.post.dto.PostTagInfo;
import com.raisedeveloper.server.domain.post.dto.PostUpdateRequest;
import com.raisedeveloper.server.domain.post.infra.PostImageRepository;
import com.raisedeveloper.server.domain.post.infra.PostLikeRepository;
import com.raisedeveloper.server.domain.post.infra.PostRepository;
import com.raisedeveloper.server.domain.post.infra.PostTagRepository;
import com.raisedeveloper.server.domain.post.infra.TagRepository;
import com.raisedeveloper.server.domain.post.mapper.PostMapper;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.domain.UserProfile;
import com.raisedeveloper.server.domain.user.infra.UserCharacterRepository;
import com.raisedeveloper.server.domain.user.infra.UserProfileRepository;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.pagination.Cursor;
import com.raisedeveloper.server.global.pagination.CursorService;
import com.raisedeveloper.server.global.pagination.PaginationConstants;
import com.raisedeveloper.server.global.pagination.PagingResponse;

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
	private final PostLikeRepository postLikeRepository;
	private final TagRepository tagRepository;
	private final PostTagRepository postTagRepository;
	private final CursorService cursorService;
	private final PostMapper postMapper;
	private final LikeCountQueryService likeCountQueryService;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public PostCreateResponse createPost(Long userId, PostCreateRequest request) {
		User user = userRepository.findById(userId)
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
		Post post = postRepository.findById(postId)
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
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
		validateAuthor(post, userId);

		postRepository.delete(post);
		postImageRepository.deleteAllByPostId(post.getId());
		postTagRepository.deleteAllByPostId(post.getId());
	}

	public PostDetailResponse getPostDetail(Long postId, Long viewerUserId) {
		Post post = postRepository.findById(postId)
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
			.map(postMapper::toTagInfo)
			.toList();

		boolean isAuthor = viewerUserId != null && viewerUserId.equals(author.getId());

		boolean isLiked = viewerUserId != null && postLikeRepository.existsByPostIdAndUserId(postId, viewerUserId);
		int likeCount = likeCountQueryService.getLikeCount(postId);

		PostAuthor postAuthor = postMapper.toPostAuthor(author.getId(), profile, character);
		PostDetail detail = postMapper.toPostDetail(post, isAuthor, postAuthor, images, tags, isLiked, likeCount);

		return new PostDetailResponse(detail);
	}

	public PostMetaDetailResponse getPostMeta(Long postId, Long viewerUserId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		Long authorId = post.getUser().getId();
		PostAuthor author = loadAuthorsByUserIds(List.of(authorId)).get(authorId);
		int likeCount = likeCountQueryService.getLikeCount(postId);
		boolean isLiked = viewerUserId != null && postLikeRepository.existsByPostIdAndUserId(postId, viewerUserId);
		boolean isAuthor = viewerUserId != null && viewerUserId.equals(authorId);

		return new PostMetaDetailResponse(
			new PostMetaItem(postId, author, likeCount, isLiked, isAuthor)
		);
	}

	public PostListResponse getPosts(Long authorId, List<String> tagNames, Integer limit, String cursor,
		Long viewerUserId) {
		if (authorId != null) {
			userRepository.findById(authorId)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		}

		List<String> normalizedTagNames = normalizeTagNames(tagNames);
		int size = normalizeLimit(limit);
		Cursor decoded = cursorService.decode(cursor);

		List<Post> posts = fetchPosts(authorId, normalizedTagNames, size, decoded);
		List<Post> sliced = posts.stream().limit(size).toList();
		List<PostListItem> items = toPostListItems(sliced, viewerUserId);

		boolean hasNext = posts.size() > size;
		String nextCursor = buildNextCursor(sliced);

		return new PostListResponse(items, new PagingResponse(nextCursor, hasNext));
	}

	public PostMetaListResponse getPostMetaList(Long authorId, List<String> tagNames, Integer limit, String cursor,
		Long viewerUserId) {
		if (authorId != null) {
			userRepository.findById(authorId)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		}

		List<String> normalizedTagNames = normalizeTagNames(tagNames);
		int size = normalizeLimit(limit);
		Cursor decoded = cursorService.decode(cursor);

		List<Post> posts = fetchPosts(authorId, normalizedTagNames, size, decoded);
		List<Post> sliced = posts.stream().limit(size).toList();
		List<PostMetaItem> items = toPostMetaItems(sliced, viewerUserId);

		boolean hasNext = posts.size() > size;
		String nextCursor = buildNextCursor(sliced);

		return new PostMetaListResponse(items, new PagingResponse(nextCursor, hasNext));
	}

	@Transactional
	public PostLikeResponse togglePostLike(Long userId, Long postId, boolean likeRequested) {
		postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
		userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		if (likeRequested) {
			int inserted = postLikeRepository.insertIgnoreByPostIdAndUserId(postId, userId);
			if (inserted > 0) {
				eventPublisher.publishEvent(new PostLikeChangedEvent(postId, 1));
			}
		} else {
			long deleted = postLikeRepository.deleteByPostIdAndUserId(postId, userId);
			if (deleted > 0) {
				eventPublisher.publishEvent(new PostLikeChangedEvent(postId, -1));
			}
		}

		return new PostLikeResponse(postId, likeRequested);
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

	private int normalizeLimit(Integer limit) {
		int normalized = (limit == null) ? PaginationConstants.POST_DEFAULT_LIMIT : limit;
		normalized = Math.max(1, normalized);
		return Math.min(PaginationConstants.POST_MAX_LIMIT, normalized);
	}

	private List<Post> fetchPosts(Long authorId, List<String> tagNames, int size, Cursor cursor) {
		PageRequest pageable = PageRequest.of(0, size + 1);
		boolean hasTagNames = !tagNames.isEmpty();
		if (authorId == null && !hasTagNames) {
			if (cursor == null) {
				return postRepository.findPage(pageable);
			}
			return postRepository.findPageByCursor(cursor.createdAt(), cursor.id(), pageable);
		}

		if (authorId == null) {
			if (cursor == null) {
				return postRepository.findPageByTagNames(tagNames, pageable);
			}
			return postRepository.findPageByTagNamesAndCursor(tagNames, cursor.createdAt(), cursor.id(), pageable);
		}

		if (!hasTagNames) {
			if (cursor == null) {
				return postRepository.findPageByAuthorId(authorId, pageable);
			}
			return postRepository.findPageByAuthorIdAndCursor(authorId, cursor.createdAt(), cursor.id(), pageable);
		}

		if (cursor == null) {
			return postRepository.findPageByAuthorIdAndTagNames(authorId, tagNames, pageable);
		}
		return postRepository.findPageByAuthorIdAndTagNamesAndCursor(authorId, tagNames,
			cursor.createdAt(),
			cursor.id(),
			pageable
		);
	}

	private String buildNextCursor(List<Post> posts) {
		if (posts.isEmpty()) {
			return null;
		}
		Post last = posts.getLast();
		return cursorService.encode(last.getCreatedAt(), last.getId());
	}

	private List<PostListItem> toPostListItems(List<Post> posts, Long viewerUserId) {
		if (posts.isEmpty()) {
			return List.of();
		}

		List<Long> userIds = posts.stream()
			.map(post -> post.getUser().getId())
			.distinct()
			.toList();
		Map<Long, PostAuthor> authorByUserId = loadAuthorsByUserIds(userIds);

		Map<Long, List<PostTagInfo>> tagsByPostId = loadTagsByPostId(posts);

		List<Long> postIds = posts.stream()
			.map(Post::getId)
			.toList();
		Set<Long> likedPostIds = loadLikedPostIds(viewerUserId, postIds);
		Map<Long, Integer> likeCountByPostId = likeCountQueryService.getLikeCounts(postIds);

		return posts.stream()
			.map(post -> {
				boolean isLiked = likedPostIds.contains(post.getId());
				return postMapper.toPostListItem(
					post,
					authorByUserId.get(post.getUser().getId()),
					tagsByPostId.getOrDefault(post.getId(), List.of()),
					isLiked,
					likeCountByPostId.getOrDefault(post.getId(), 0)
				);
			})
			.toList();
	}

	private List<PostMetaItem> toPostMetaItems(List<Post> posts, Long viewerUserId) {
		if (posts.isEmpty()) {
			return List.of();
		}

		List<Long> userIds = posts.stream()
			.map(post -> post.getUser().getId())
			.distinct()
			.toList();
		Map<Long, PostAuthor> authorByUserId = loadAuthorsByUserIds(userIds);

		List<Long> postIds = posts.stream()
			.map(Post::getId)
			.toList();
		Set<Long> likedPostIds = loadLikedPostIds(viewerUserId, postIds);
		Map<Long, Integer> likeCountByPostId = likeCountQueryService.getLikeCounts(postIds);

		return posts.stream()
			.map(post -> new PostMetaItem(
				post.getId(),
				authorByUserId.get(post.getUser().getId()),
				likeCountByPostId.getOrDefault(post.getId(), 0),
				likedPostIds.contains(post.getId()),
				null
			))
			.toList();
	}

	private Map<Long, PostAuthor> loadAuthorsByUserIds(List<Long> userIds) {
		Map<Long, UserProfile> profileByUserId = userProfileRepository.findByUserIdIn(userIds).stream()
			.collect(Collectors.toMap(profile -> profile.getUser().getId(), profile -> profile));
		if (profileByUserId.size() != userIds.size()) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}

		Map<Long, UserCharacter> characterByUserId = userCharacterRepository.findByUserIdIn(userIds).stream()
			.collect(Collectors.toMap(character -> character.getUser().getId(), character -> character));
		if (characterByUserId.size() != userIds.size()) {
			throw new CustomException(ErrorCode.CHARACTER_NOT_SET);
		}

		Map<Long, PostAuthor> authorByUserId = new HashMap<>();
		for (Long userId : userIds) {
			UserProfile profile = profileByUserId.get(userId);
			UserCharacter character = characterByUserId.get(userId);
			authorByUserId.put(userId, postMapper.toPostAuthor(userId, profile, character));
		}
		return authorByUserId;
	}

	private Set<Long> loadLikedPostIds(Long viewerUserId, List<Long> postIds) {
		if (viewerUserId == null || postIds.isEmpty()) {
			return Set.of();
		}
		return Set.copyOf(postLikeRepository.findPostIdsByUserIdAndPostIdIn(viewerUserId, postIds));
	}

	private Map<Long, List<PostTagInfo>> loadTagsByPostId(List<Post> posts) {
		List<Long> postIds = posts.stream()
			.map(Post::getId)
			.toList();
		if (postIds.isEmpty()) {
			return Map.of();
		}

		Map<Long, List<PostTagInfo>> tagsByPostId = new LinkedHashMap<>();
		List<PostTag> postTags = postTagRepository.findByPostIdIn(postIds);
		for (PostTag postTag : postTags) {
			Long postId = postTag.getPost().getId();
			PostTagInfo info = postMapper.toTagInfo(postTag);
			tagsByPostId.computeIfAbsent(postId, key -> new ArrayList<>()).add(info);
		}
		return tagsByPostId;
	}

	private List<String> normalizeTagNames(List<String> tagNames) {
		List<String> normalized = normalizeDistinctList(tagNames);
		if (normalized.size() > 5) {
			throw new CustomException(ErrorCode.VALIDATION_FAILED);
		}
		return normalized;
	}
}
