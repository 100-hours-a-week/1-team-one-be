package com.raisedeveloper.server.global.consumer.infra;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.global.consumer.domain.ConsumedEvent;

public interface ConsumedEventRepository extends JpaRepository<ConsumedEvent, Long> {

	@Modifying
	@Query(
		value = "INSERT IGNORE INTO consumed_events (consumer_name, event_id, created_at) "
			+ "VALUES (:consumerName, :eventId, :createdAt)",
		nativeQuery = true
	)
	int insertIgnore(
		@Param("consumerName") String consumerName,
		@Param("eventId") String eventId,
		@Param("createdAt") LocalDateTime createdAt
	);
}
