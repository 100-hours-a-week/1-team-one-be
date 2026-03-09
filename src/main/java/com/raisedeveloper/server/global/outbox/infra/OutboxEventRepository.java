package com.raisedeveloper.server.global.outbox.infra;

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

	@Query(value = """
		SELECT id
		FROM outbox_events
		WHERE status = 'PENDING'
		ORDER BY id ASC
		LIMIT :limit
		FOR UPDATE SKIP LOCKED
		""", nativeQuery = true)
	List<Long> findPendingIdsForUpdateSkipLocked(@Param("limit") int limit);

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

}
