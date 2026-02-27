package com.raisedeveloper.server.global.consumer.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.global.consumer.infra.ConsumedEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsumerIdempotencyService {

	private final ConsumedEventRepository consumedEventRepository;

	@Transactional
	public boolean markProcessedIfFirst(String consumerName, String eventId) {
		return consumedEventRepository.insertIgnore(consumerName, eventId, LocalDateTime.now()) == 1;
	}
}
