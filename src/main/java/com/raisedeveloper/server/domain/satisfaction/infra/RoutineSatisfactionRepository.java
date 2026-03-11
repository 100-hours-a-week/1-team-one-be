package com.raisedeveloper.server.domain.satisfaction.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.satisfaction.domain.RoutineSatisfaction;

@Repository
public interface RoutineSatisfactionRepository extends JpaRepository<RoutineSatisfaction, Long> {

	Optional<RoutineSatisfaction> findByUserIdAndRoutineId(Long userId, Long routineId);
}
