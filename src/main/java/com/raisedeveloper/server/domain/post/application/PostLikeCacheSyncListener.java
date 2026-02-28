package com.raisedeveloper.server.domain.post.application;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.raisedeveloper.server.domain.post.infra.PostLikeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostLikeCacheSyncListener {

	private final LikeCountCacheService likeCountCacheService;
	private final PostLikeRepository postLikeRepository;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void syncLikeCount(PostLikeChangedEvent event) {
		boolean incremented = likeCountCacheService.incrementIfPresent(event.postId(), event.delta());
		if (incremented) {
			return;
		}

		long exactCount = postLikeRepository.countByPostId(event.postId());
		likeCountCacheService.setLikeCount(event.postId(), exactCount);
	}
}
