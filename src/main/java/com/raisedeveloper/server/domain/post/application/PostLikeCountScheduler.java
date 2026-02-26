package com.raisedeveloper.server.domain.post.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import com.raisedeveloper.server.domain.post.domain.PostLikeOutbox;
import com.raisedeveloper.server.domain.post.infra.PostLikeOutboxRepository;
import com.raisedeveloper.server.domain.post.infra.PostRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PostLikeCountScheduler {

	private final PostRepository postRepository;
	private final PostLikeOutboxRepository postLikeOutboxRepository;

	@Scheduled(fixedDelay = 600_000L)
	@SchedulerLock(name = "PostLikeCountScheduler.refreshLikeCounts", lockAtMostFor = "PT15M")
	@Transactional
	public void refreshLikeCounts() {
		List<PostLikeOutbox> events = postLikeOutboxRepository.findTop1000ByProcessedAtIsNullOrderByIdAsc();
		if (events.isEmpty()) {
			return;
		}

		Map<Long, Long> deltaByPostId = events.stream()
			.collect(Collectors.groupingBy(
				PostLikeOutbox::getPostId,
				Collectors.summingLong(PostLikeOutbox::getDelta)
			));

		for (Map.Entry<Long, Long> entry : deltaByPostId.entrySet()) {
			postRepository.updateLikeCountDelta(entry.getKey(), entry.getValue());
		}

		LocalDateTime processedAt = LocalDateTime.now();
		List<Long> ids = events.stream()
			.map(PostLikeOutbox::getId)
			.toList();
		postLikeOutboxRepository.markProcessed(processedAt, ids);
	}
}
