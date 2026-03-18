package com.raisedeveloper.server.global.outbox.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.outbox.domain.OutboxAggregateType;
import com.raisedeveloper.server.global.outbox.domain.OutboxEvent;
import com.raisedeveloper.server.global.outbox.domain.OutboxEventType;
import com.raisedeveloper.server.global.outbox.infra.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventStore {

	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	@Transactional
	public void store(
		String eventId,
		OutboxAggregateType aggregateType,
		String aggregateId,
		String topic,
		OutboxEventType eventType,
		String messageKey,
		Object payload
	) {
		try {
			String payloadJson = objectMapper.writeValueAsString(payload);
			outboxEventRepository.save(new OutboxEvent(
				eventId,
				aggregateType.getValue(),
				aggregateId,
				topic,
				eventType.getValue(),
				messageKey,
				payloadJson
			));
		} catch (JsonProcessingException e) {
			log.error(
				"Outbox payload serialization failed: eventId={}, aggregateType={}, aggregateId={}, topic={}, eventType={}, messageKey={}, payloadType={}",
				eventId,
				aggregateType,
				aggregateId,
				topic,
				eventType,
				messageKey,
				payload == null ? "null" : payload.getClass().getName(),
				e
			);
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public void storeBatch(List<OutboxStoreCommand> commands) {
		if (commands == null || commands.isEmpty()) {
			return;
		}

		List<OutboxEvent> outboxEvents = new ArrayList<>(commands.size());
		try {
			for (OutboxStoreCommand command : commands) {
				String payloadJson = objectMapper.writeValueAsString(command.payload());
				outboxEvents.add(new OutboxEvent(
					command.eventId(),
					command.aggregateType().getValue(),
					command.aggregateId(),
					command.topic(),
					command.eventType().getValue(),
					command.messageKey(),
					payloadJson
				));
			}
			outboxEventRepository.saveAll(outboxEvents);
		} catch (JsonProcessingException e) {
			log.error("Outbox batch payload serialization failed: size={}", commands.size(), e);
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public record OutboxStoreCommand(
		String eventId,
		OutboxAggregateType aggregateType,
		String aggregateId,
		String topic,
		OutboxEventType eventType,
		String messageKey,
		Object payload
	) {
	}
}
