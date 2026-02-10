package com.raisedeveloper.server.domain.routine.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raisedeveloper.server.domain.routine.domain.RoutineGenerationJob;

public interface RoutineGenerationJobRepository extends JpaRepository<RoutineGenerationJob, Long> {
	Optional<RoutineGenerationJob> findByJobId(String jobId);
}
