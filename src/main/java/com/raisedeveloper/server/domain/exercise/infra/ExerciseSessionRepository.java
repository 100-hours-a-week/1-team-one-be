package com.raisedeveloper.server.domain.exercise.infra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
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
		+ "JOIN FETCH es.user u "
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

	@Query("SELECT COUNT(es) "
		+ "FROM ExerciseSession es "
		+ "WHERE es.user.id = :userId "
		+ "AND es.isRoutineCompleted = true "
		+ "AND es.startAt >= :startAt "
		+ "AND es.startAt < :endAt")
	long countCompletedInRange(
		@Param("userId") Long userId,
		@Param("startAt") LocalDateTime startAt,
		@Param("endAt") LocalDateTime endAt
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
		+ "AND es.startAt IS NULL "
		+ "AND es.createdAt <= :cutoff "
		+ "ORDER BY es.createdAt ASC, es.id ASC")
	List<ExerciseSession> findStaleUnupdatedSessions(
		@Param("cutoff") LocalDateTime cutoff,
		Pageable pageable
	);

	@Query("SELECT es.user.id as userId, MAX(es.createdAt) as lastCreatedAt "
		+ "FROM ExerciseSession es "
		+ "WHERE es.user.id IN :userIds "
		+ "GROUP BY es.user.id")
	List<UserLastSessionProjection> findLatestSessionsByUserIds(@Param("userIds") List<Long> userIds);

	@Query(value = """
		SELECT
			es.user_id AS userId,
			ROUND(
				SUM(CASE WHEN es.start_at IS NOT NULL THEN 1 ELSE 0 END) / COUNT(es.id),
				2
			) AS weeklyFrequency
		FROM exercise_sessions es
		WHERE es.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
			AND es.user_id IN (:userIds)
		GROUP BY es.user_id
		""", nativeQuery = true)
	List<WeeklyFrequencyProjection> findWeeklyFrequenciesByUserIds(@Param("userIds") List<Long> userIds);

	@Query(value = """
		SELECT ROUND(AVG(TIMESTAMPDIFF(SECOND, es.created_at, es.start_at)))
		FROM exercise_sessions es
		WHERE es.user_id = :userId
			AND es.start_at IS NOT NULL
			AND es.start_at >= es.created_at
			AND (:startDate IS NULL OR es.created_at >= :startDate)
			AND (:endDate IS NULL OR es.created_at < :endDate)
		""", nativeQuery = true)
	Long findAverageReactionSecondsByUserId(
		@Param("userId") Long userId,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	@Query(value = """
		SELECT ranked.rank_no
		FROM (
			SELECT
				user_id,
				DENSE_RANK() OVER (
					ORDER BY AVG(TIMESTAMPDIFF(SECOND, created_at, start_at)) ASC
				) AS rank_no
			FROM exercise_sessions
			WHERE start_at IS NOT NULL
				AND start_at >= created_at
				AND (:startDate IS NULL OR created_at >= :startDate)
				AND (:endDate IS NULL OR created_at < :endDate)
			GROUP BY user_id
		) ranked
		WHERE ranked.user_id = :userId
		""", nativeQuery = true)
	Integer findReactionSpeedRankByUserId(
		@Param("userId") Long userId,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	@Query(value = """
		SELECT COUNT(*)
		FROM (
			SELECT es.user_id
			FROM exercise_sessions es
			WHERE es.start_at IS NOT NULL
				AND es.start_at >= es.created_at
				AND (:startDate IS NULL OR es.created_at >= :startDate)
				AND (:endDate IS NULL OR es.created_at < :endDate)
			GROUP BY es.user_id
		) ranked_users
		""", nativeQuery = true)
	long countReactionSpeedRankedUsers(
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	interface UserLastSessionProjection {
		Long getUserId();

		LocalDateTime getLastCreatedAt();
	}

	interface WeeklyFrequencyProjection {
		Long getUserId();

		BigDecimal getWeeklyFrequency();
	}
}
