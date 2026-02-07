package com.raisedeveloper.server.domain.routine.infra;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.routine.domain.RoutineStep;

@Repository
public interface RoutineStepRepository extends JpaRepository<RoutineStep, Long> {

	@EntityGraph(attributePaths = {"routine", "exercise"})
	List<RoutineStep> findAllByRoutineIdInOrderByIdAsc(
		List<Long> routineIds
	);

	@Query("SELECT rs FROM RoutineStep rs "
		+ "JOIN FETCH rs.exercise e "
		+ "WHERE rs.routine.id = :routineId "
		+ "ORDER BY rs.stepOrder")
	List<RoutineStep> findByRoutineIdWithExercise(@Param("routineId") Long routineId);
}
