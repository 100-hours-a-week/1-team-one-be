package com.raisedeveloper.server.global.outbox.application;

import java.time.LocalDateTime;
import java.util.List;

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
		List<Long> ids = outboxEventRepository.findPendingIdsForUpdateSkipLocked(batchSize);
		if (ids.isEmpty()) {
			return List.of();
		}
		outboxEventRepository.updateStatusBatch(ids, OutboxStatus.PROCESSING);
		return ids;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markPublished(Long outboxId) {
		OutboxEvent outboxEvent = outboxEventRepository.findById(outboxId)
			.orElseThrow();
		outboxEvent.markPublished(LocalDateTime.now());
		outboxEventRepository.save(outboxEvent);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markRetry(Long outboxId, String errorMessage) {
		OutboxEvent outboxEvent = outboxEventRepository.findById(outboxId)
			.orElseThrow();
		outboxEvent.markRetry(errorMessage == null ? "unknown" : truncate(errorMessage));
		outboxEventRepository.save(outboxEvent);
	}

	private String truncate(String value) {
		return value.length() <= 1000 ? value : value.substring(0, 1000);
	}
}
