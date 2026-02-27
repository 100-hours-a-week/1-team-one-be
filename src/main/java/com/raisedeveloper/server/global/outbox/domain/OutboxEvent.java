package com.raisedeveloper.server.global.outbox.domain;

import java.time.LocalDateTime;

import com.raisedeveloper.server.domain.common.domain.CreatedUpdatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "outbox_events")
public class OutboxEvent extends CreatedUpdatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 64)
	private String eventId;

	@Column(nullable = false, length = 100)
	private String aggregateType;

	@Column(nullable = false, length = 100)
	private String aggregateId;

	@Column(nullable = false, length = 200)
	private String topic;

	@Column(length = 150)
	private String eventType;

	@Column(nullable = false, length = 100)
	private String messageKey;

	@Column(nullable = false)
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OutboxStatus status;

	private LocalDateTime publishedAt;

	@Column(nullable = false)
	private int retryCount;

	private String lastError;

	public OutboxEvent(
		String eventId,
		String aggregateType,
		String aggregateId,
		String topic,
		String eventType,
		String messageKey,
		String payload
	) {
		this.eventId = eventId;
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.topic = topic;
		this.eventType = eventType;
		this.messageKey = messageKey;
		this.payload = payload;
		this.status = OutboxStatus.PENDING;
		this.retryCount = 0;
	}

	public void markPublished(LocalDateTime publishedAt) {
		this.status = OutboxStatus.PUBLISHED;
		this.publishedAt = publishedAt;
		this.lastError = null;
	}

	public void markRetry(String errorMessage) {
		this.retryCount += 1;
		this.lastError = errorMessage;
	}
}
