package com.raisedeveloper.server.domain.exercise.infra;

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
}
