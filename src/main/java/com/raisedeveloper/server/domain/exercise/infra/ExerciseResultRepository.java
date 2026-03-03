package com.raisedeveloper.server.domain.exercise.infra;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseResult;

@Repository
public interface ExerciseResultRepository extends JpaRepository<ExerciseResult, Long> {

	@Query("SELECT er FROM ExerciseResult er "
		+ "JOIN FETCH er.routineStep rs "
		+ "JOIN FETCH rs.exercise e "
		+ "WHERE er.exerciseSession.id = :sessionId "
		+ "ORDER BY rs.stepOrder")
	List<ExerciseResult> findByExerciseSessionIdWithDetails(@Param("sessionId") Long sessionId);

	@Query("SELECT er FROM ExerciseResult er "
		+ "WHERE er.exerciseSession.id IN :sessionIds")
	List<ExerciseResult> findByExerciseSessionIds(@Param("sessionIds") List<Long> sessionIds);

	@Query(value = """
		SELECT
		    es.user_id AS userId,
		    e.body_part AS bodyPart,
		    ROUND(
		        COUNT(*) / SUM(COUNT(*)) OVER (PARTITION BY es.user_id),
		        2
		    ) AS ratio
		FROM exercise_results er
		JOIN exercise_sessions es ON er.exercise_session_id = es.id
		JOIN routine_steps rs ON er.routine_step_id = rs.id
		JOIN exercises e ON rs.exercise_id = e.id
		WHERE er.status = 'COMPLETED'
		  AND es.user_id IN (:userIds)
		GROUP BY es.user_id, e.body_part
		ORDER BY es.user_id, e.body_part
		""", nativeQuery = true)
	List<BodyPartRatioProjection> findBodyPartRatiosByUserIds(@Param("userIds") List<Long> userIds);

	@Query(value = """
		SELECT
		    es.user_id AS userId,
		    e.type AS exerciseType,
		    ROUND(
		        COUNT(*) / SUM(COUNT(*)) OVER (PARTITION BY es.user_id),
		        2
		    ) AS ratio
		FROM exercise_results er
		JOIN exercise_sessions es ON er.exercise_session_id = es.id
		JOIN routine_steps rs ON er.routine_step_id = rs.id
		JOIN exercises e ON rs.exercise_id = e.id
		WHERE er.status = 'COMPLETED'
		  AND es.user_id IN (:userIds)
		GROUP BY es.user_id, e.type
		ORDER BY es.user_id, e.type
		""", nativeQuery = true)
	List<ExerciseTypeRatioProjection> findExerciseTypeRatiosByUserIds(@Param("userIds") List<Long> userIds);

	@Query(value = """
		SELECT
		    es.user_id AS userId,
		    e.difficulty AS difficulty,
		    ROUND(
		        COUNT(*) / SUM(COUNT(*)) OVER (PARTITION BY es.user_id),
		        2
		    ) AS ratio
		FROM exercise_results er
		JOIN exercise_sessions es ON er.exercise_session_id = es.id
		JOIN routine_steps rs ON er.routine_step_id = rs.id
		JOIN exercises e ON rs.exercise_id = e.id
		WHERE er.status = 'COMPLETED'
		  AND es.user_id IN (:userIds)
		GROUP BY es.user_id, e.difficulty
		ORDER BY es.user_id, e.difficulty
		""", nativeQuery = true)
	List<DifficultyRatioProjection> findDifficultyRatiosByUserIds(@Param("userIds") List<Long> userIds);

	interface BodyPartRatioProjection {
		Long getUserId();

		String getBodyPart();

		BigDecimal getRatio();
	}

	interface ExerciseTypeRatioProjection {
		Long getUserId();

		String getExerciseType();

		BigDecimal getRatio();
	}

	interface DifficultyRatioProjection {
		Long getUserId();

		Byte getDifficulty();

		BigDecimal getRatio();
	}
}
