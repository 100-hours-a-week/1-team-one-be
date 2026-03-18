package com.raisedeveloper.server.global.outbox.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.global.outbox.domain.OutboxEvent;
import com.raisedeveloper.server.global.outbox.domain.OutboxStatus;
import com.raisedeveloper.server.global.outbox.infra.OutboxEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutboxStateService {
	private final OutboxEventRepository outboxEventRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Long> claimPendingBatchIds(int batchSize) {
		List<Long> ids = outboxEventRepository.findByStatusOrderByIdAsc(
			OutboxStatus.PENDING,
			PageRequest.of(0, batchSize)
		).stream()
			.map(OutboxEvent::getId)
			.toList();
		if (ids.isEmpty()) {
			return List.of();
		}
		outboxEventRepository.updateStatusBatch(ids, OutboxStatus.PROCESSING);
		return ids;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markPublishedBatch(List<Long> outboxIds) {
		if (outboxIds == null || outboxIds.isEmpty()) {
			return;
		}
		outboxEventRepository.markPublishedBatch(outboxIds, LocalDateTime.now());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markRetryBatch(List<Long> outboxIds, String errorMessage) {
		if (outboxIds == null || outboxIds.isEmpty()) {
			return;
		}
		outboxEventRepository.markRetryBatch(outboxIds, errorMessage == null ? "unknown" : truncate(errorMessage));
	}

	private String truncate(String value) {
		return value.length() <= 1000 ? value : value.substring(0, 1000);
	}
}
