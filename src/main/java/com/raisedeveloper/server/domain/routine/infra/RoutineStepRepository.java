package com.raisedeveloper.server.domain.routine.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.routine.domain.RoutineStep;

@Repository
public interface RoutineStepRepository extends JpaRepository<RoutineStep, Long> {
	List<RoutineStep> findAllByRoutineIdIn(
		List<Long> routineIds
	);
}
