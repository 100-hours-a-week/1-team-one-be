package com.raisedeveloper.server.global.outbox.infra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.global.outbox.domain.OutboxEvent;
import com.raisedeveloper.server.global.outbox.domain.OutboxStatus;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

	List<OutboxEvent> findByStatusOrderByIdAsc(OutboxStatus status, Pageable pageable);

	List<OutboxEvent> findByIdInOrderByIdAsc(List<Long> ids);

	@Modifying
	@Query("""
		UPDATE OutboxEvent o
		SET o.status = :status
		WHERE o.id IN :outboxIds
		""")
	int updateStatusBatch(
		@Param("outboxIds") List<Long> outboxIds,
		@Param("status") OutboxStatus status
	);

	@Modifying
	@Query("""
		UPDATE OutboxEvent o
		SET o.status = com.raisedeveloper.server.global.outbox.domain.OutboxStatus.PUBLISHED,
			o.publishedAt = :publishedAt,
			o.lastError = null
		WHERE o.id IN :outboxIds
		""")
	int markPublishedBatch(
		@Param("outboxIds") List<Long> outboxIds,
		@Param("publishedAt") LocalDateTime publishedAt
	);

	@Modifying
	@Query("""
		UPDATE OutboxEvent o
		SET o.status = com.raisedeveloper.server.global.outbox.domain.OutboxStatus.PENDING,
			o.retryCount = o.retryCount + 1,
			o.lastError = :errorMessage
		WHERE o.id IN :outboxIds
		""")
	int markRetryBatch(
		@Param("outboxIds") List<Long> outboxIds,
		@Param("errorMessage") String errorMessage
	);

}
