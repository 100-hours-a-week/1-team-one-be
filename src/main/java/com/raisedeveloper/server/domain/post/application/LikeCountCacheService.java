package com.raisedeveloper.server.domain.post.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeCountCacheService {

	private static final String KEY_PREFIX = "post:like:count:";

	private static final DefaultRedisScript<Long> INCREMENT_IF_PRESENT_SCRIPT = new DefaultRedisScript<>(
		"""
		if redis.call('EXISTS', KEYS[1]) == 0 then
			return 0
		end
		local next = redis.call('INCRBY', KEYS[1], ARGV[1])
		if next < 0 then
			redis.call('SET', KEYS[1], '0')
			return 1
		end
		return 1
		""",
		Long.class
	);

	private final StringRedisTemplate redisTemplate;

	public Optional<Integer> getLikeCount(Long postId) {
		String raw = redisTemplate.opsForValue().get(key(postId));
		if (raw == null) {
			return Optional.empty();
		}
		return Optional.of(parseCount(raw));
	}

	public Map<Long, Integer> getLikeCounts(List<Long> postIds) {
		if (postIds.isEmpty()) {
			return Map.of();
		}

		List<String> keys = postIds.stream()
			.map(this::key)
			.toList();
		List<String> values = redisTemplate.opsForValue().multiGet(keys);
		if (values == null) {
			return Map.of();
		}

		Map<Long, Integer> result = new HashMap<>();
		for (int i = 0; i < postIds.size(); i++) {
			String value = values.get(i);
			if (value != null) {
				result.put(postIds.get(i), parseCount(value));
			}
		}
		return result;
	}

	public void setLikeCount(Long postId, long count) {
		redisTemplate.opsForValue().set(key(postId), Long.toString(sanitizeCount(count)));
	}

	public void setLikeCounts(Map<Long, Long> countByPostId) {
		for (Map.Entry<Long, Long> entry : countByPostId.entrySet()) {
			setLikeCount(entry.getKey(), entry.getValue());
		}
	}

	public boolean incrementIfPresent(Long postId, int delta) {
		Long result = redisTemplate.execute(
			INCREMENT_IF_PRESENT_SCRIPT,
			List.of(key(postId)),
			Integer.toString(delta)
		);
		return Long.valueOf(1L).equals(result);
	}

	private String key(Long postId) {
		return KEY_PREFIX + postId;
	}

	private int parseCount(String raw) {
		try {
			return (int)Math.min(Integer.MAX_VALUE, Math.max(0L, Long.parseLong(raw)));
		} catch (NumberFormatException ignored) {
			return 0;
		}
	}

	private long sanitizeCount(long count) {
		return Math.max(0L, count);
	}
}
