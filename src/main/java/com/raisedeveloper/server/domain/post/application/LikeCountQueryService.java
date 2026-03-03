package com.raisedeveloper.server.domain.post.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.post.infra.PostLikeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeCountQueryService {

	private final LikeCountCacheService likeCountCacheService;
	private final PostLikeRepository postLikeRepository;

	public int getLikeCount(Long postId) {
		return likeCountCacheService.getLikeCount(postId)
			.orElseGet(() -> {
				long count = postLikeRepository.countByPostId(postId);
				likeCountCacheService.setLikeCount(postId, count);
				return toResponseCount(count);
			});
	}

	public Map<Long, Integer> getLikeCounts(List<Long> postIds) {
		if (postIds.isEmpty()) {
			return Map.of();
		}

		Map<Long, Integer> cached = likeCountCacheService.getLikeCounts(postIds);
		if (cached.size() == postIds.size()) {
			return cached;
		}

		List<Long> missingPostIds = new ArrayList<>();
		for (Long postId : postIds) {
			if (!cached.containsKey(postId)) {
				missingPostIds.add(postId);
			}
		}
		if (missingPostIds.isEmpty()) {
			return cached;
		}

		Map<Long, Long> dbCountByPostId = new HashMap<>();
		postLikeRepository.countLikeCountsByPostIds(missingPostIds)
			.forEach(projection -> dbCountByPostId.put(projection.getPostId(), projection.getLikeCount()));

		for (Long postId : missingPostIds) {
			dbCountByPostId.putIfAbsent(postId, 0L);
		}
		likeCountCacheService.setLikeCounts(dbCountByPostId);

		Map<Long, Integer> merged = new HashMap<>(cached);
		dbCountByPostId.forEach((postId, count) -> merged.put(postId, toResponseCount(count)));
		return merged;
	}

	private int toResponseCount(long count) {
		return (int)Math.min(Integer.MAX_VALUE, Math.max(0L, count));
	}
}
