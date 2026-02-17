package com.raisedeveloper.server.domain.post.infra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.domain.post.domain.PostLikeOutbox;

public interface PostLikeOutboxRepository extends JpaRepository<PostLikeOutbox, Long> {

	List<PostLikeOutbox> findTop1000ByProcessedAtIsNullOrderByIdAsc();

	@Modifying
	@Query("update PostLikeOutbox o set o.processedAt = :processedAt where o.id in :ids")
	void markProcessed(@Param("processedAt") LocalDateTime processedAt, @Param("ids") List<Long> ids);
}
