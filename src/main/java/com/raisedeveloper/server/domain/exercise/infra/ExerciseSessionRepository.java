package com.raisedeveloper.server.domain.exercise.infra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.stats.dto.GrassStatsProjection;

@Repository
public interface ExerciseSessionRepository extends JpaRepository<ExerciseSession, Long> {

	@Query("SELECT es FROM ExerciseSession es "
		+ "WHERE es.user.id = :userId "
		+ "ORDER BY es.createdAt DESC "
		+ "LIMIT 1")
	Optional<ExerciseSession> findLatestByUserId(@Param("userId") Long userId);

	@Query("SELECT es FROM ExerciseSession es "
		+ "JOIN FETCH es.routine r "
		+ "WHERE es.id = :sessionId AND es.user.id = :userId")
	Optional<ExerciseSession> findByIdAndUserIdWithRoutine(
		@Param("sessionId") Long sessionId,
		@Param("userId") Long userId
	);

	@EntityGraph(attributePaths = "routine")
	List<ExerciseSession> findByUserIdAndIsRoutineCompletedIsNullOrderByCreatedAtDesc(
		Long userId);

	@Query("SELECT CASE WHEN COUNT(es) > 0 THEN true ELSE false END "
		+ "FROM ExerciseSession es "
		+ "WHERE es.user.id = :userId "
		+ "AND es.isRoutineCompleted = true "
		+ "AND es.startAt >= :startAt "
		+ "AND es.startAt < :endAt")
	boolean existsCompletedInRange(
		@Param("userId") Long userId,
		@Param("startAt") java.time.LocalDateTime startAt,
		@Param("endAt") java.time.LocalDateTime endAt
	);

	@Query("SELECT CAST(es.createdAt AS LocalDate) as date, "
		+ "COUNT(es) as targetCount, "
		+ "SUM(CASE WHEN es.isRoutineCompleted = true THEN 1 ELSE 0 END) as successCount "
		+ "FROM ExerciseSession es "
		+ "WHERE es.user.id = :userId "
		+ "AND es.isRoutineCompleted IS NOT NULL "
		+ "AND es.createdAt >= :startDate "
		+ "AND es.createdAt < :endDate "
		+ "GROUP BY CAST(es.createdAt AS LocalDate) "
		+ "ORDER BY date ASC")
	List<GrassStatsProjection> findGrassStatsByDateRange(
		@Param("userId") Long userId,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	@Query("SELECT es FROM ExerciseSession es "
		+ "WHERE es.isRoutineCompleted IS NULL "
		+ "AND es.updatedAt = es.createdAt "
		+ "AND es.createdAt <= :cutoff")
	List<ExerciseSession> findStaleUnupdatedSessions(@Param("cutoff") LocalDateTime cutoff);

	@Query("SELECT es.user.id as userId, MAX(es.createdAt) as lastCreatedAt "
		+ "FROM ExerciseSession es "
		+ "WHERE es.user.id IN :userIds "
		+ "GROUP BY es.user.id")
	List<UserLastSessionProjection> findLatestSessionsByUserIds(@Param("userIds") List<Long> userIds);

	interface UserLastSessionProjection {
		Long getUserId();

		LocalDateTime getLastCreatedAt();
	}
}
