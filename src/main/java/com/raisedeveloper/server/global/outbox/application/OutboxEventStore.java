package com.raisedeveloper.server.global.outbox.application;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.outbox.domain.OutboxAggregateType;
import com.raisedeveloper.server.global.outbox.domain.OutboxEvent;
import com.raisedeveloper.server.global.outbox.domain.OutboxEventType;
import com.raisedeveloper.server.global.outbox.domain.OutboxStatus;
import com.raisedeveloper.server.global.outbox.infra.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventStore {

	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;
	private final JdbcTemplate jdbcTemplate;

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

		try {
			List<String> payloads = commands.stream()
				.map(command -> {
					try {
						return objectMapper.writeValueAsString(command.payload());
					} catch (JsonProcessingException e) {
						throw new PayloadSerializationException(e);
					}
				})
				.toList();

			Timestamp now = Timestamp.valueOf(java.time.LocalDateTime.now());
			jdbcTemplate.batchUpdate("""
				INSERT INTO outbox_events
				(event_id, aggregate_type, aggregate_id, topic, event_type, message_key, payload, status, published_at, retry_count, last_error, created_at, updated_at)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""",
				new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int num) throws SQLException {
						OutboxStoreCommand command = commands.get(num);
						ps.setString(1, command.eventId());
						ps.setString(2, command.aggregateType().getValue());
						ps.setString(3, command.aggregateId());
						ps.setString(4, command.topic());
						ps.setString(5, command.eventType().getValue());
						ps.setString(6, command.messageKey());
						ps.setString(7, payloads.get(num));
						ps.setString(8, OutboxStatus.PENDING.name());
						ps.setTimestamp(9, null);
						ps.setInt(10, 0);
						ps.setString(11, null);
						ps.setTimestamp(12, now);
						ps.setTimestamp(13, now);
					}

					@Override
					public int getBatchSize() {
						return commands.size();
					}
				}
			);
		} catch (PayloadSerializationException e) {
			log.error("Outbox batch payload serialization failed: size={}", commands.size(), e.getCause());
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private static final class PayloadSerializationException extends RuntimeException {
		private PayloadSerializationException(JsonProcessingException cause) {
			super(cause);
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
