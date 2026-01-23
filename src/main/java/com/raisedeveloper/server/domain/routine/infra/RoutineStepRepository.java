package com.raisedeveloper.server.domain.routine.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.routine.domain.RoutineStep;

@Repository
public interface RoutineStepRepository extends JpaRepository<RoutineStep, Long> {
}
