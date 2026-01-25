package com.raisedeveloper.server.domain.routine.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.routine.domain.Routine;

@Repository
public interface RoutineRepository extends JpaRepository<Routine, Long> {
	List<Routine> findAllByUserIdAndIsActiveTrue(Long userId);
}
