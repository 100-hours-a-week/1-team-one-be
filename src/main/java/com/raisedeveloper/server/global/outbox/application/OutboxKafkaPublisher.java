package com.raisedeveloper.server.global.outbox.application;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OutboxKafkaPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public CompletableFuture<SendResult<String, String>> publish(String topic, String key, String payload) {
		return kafkaTemplate.send(topic, key, payload);
	}
}
