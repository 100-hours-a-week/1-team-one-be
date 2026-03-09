package com.raisedeveloper.server.global.outbox.application;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.raisedeveloper.server.global.outbox.domain.OutboxEvent;
import com.raisedeveloper.server.global.outbox.infra.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxRelayService {

	private final OutboxEventRepository outboxEventRepository;
	private final OutboxKafkaPublisher outboxKafkaPublisher;
	private final OutboxStateService outboxStateService;

	public void relayPendingBatch(int batchSize) {
		List<Long> claimedIds = outboxStateService.claimPendingBatchIds(batchSize);
		if (claimedIds.isEmpty()) {
			return;
		}

		List<OutboxEvent> pendingEvents = outboxEventRepository.findByIdInOrderByIdAsc(claimedIds);
		if (pendingEvents.isEmpty()) {
			return;
		}
		List<PendingPublish> pendingPublishes = new ArrayList<>(pendingEvents.size());

		for (OutboxEvent outboxEvent : pendingEvents) {
			CompletableFuture<SendResult<String, String>> future = outboxKafkaPublisher.publish(
				outboxEvent.getTopic(),
				outboxEvent.getMessageKey(),
				outboxEvent.getPayload()
			);
			pendingPublishes.add(new PendingPublish(outboxEvent, future));
		}

		CompletableFuture<?>[] futures = pendingPublishes.stream()
			.map(PendingPublish::future)
			.toArray(CompletableFuture[]::new);
		try {
			CompletableFuture.allOf(futures).join();
		} catch (CompletionException ignored) {
			// Handle each publish result below.
		}

		for (PendingPublish pendingPublish : pendingPublishes) {
			OutboxEvent outboxEvent = pendingPublish.outboxEvent();
			try {
				pendingPublish.future().join();
				outboxStateService.markPublished(outboxEvent.getId());
			} catch (CompletionException e) {
				Throwable cause = e.getCause() == null ? e : e.getCause();
				outboxStateService.markRetry(outboxEvent.getId(), cause.getMessage());
				log.error("Outbox publish failed: outboxId={}, topic={}, eventId={}",
					outboxEvent.getId(), outboxEvent.getTopic(), outboxEvent.getEventId(), cause);
			}
		}
	}

	private record PendingPublish(
		OutboxEvent outboxEvent,
		CompletableFuture<SendResult<String, String>> future
	) {
	}
}
