package com.raisedeveloper.server.global.outbox.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.raisedeveloper.server.global.outbox.application.OutboxRelayService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

	private final OutboxRelayService outboxRelayService;

	@Value("${app.outbox.relay.batch-size:100}")
	private int batchSize;

	@Scheduled(
		fixedDelayString = "${app.outbox.relay.fixed-delay-ms:1000}",
		scheduler = "outboxTaskScheduler"
	)
	public void relay() {
		outboxRelayService.relayPendingBatch(batchSize);
	}
}
