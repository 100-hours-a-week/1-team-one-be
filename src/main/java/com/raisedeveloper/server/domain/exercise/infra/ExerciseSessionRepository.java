package com.raisedeveloper.server.domain.exercise.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;

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
}
