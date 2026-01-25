package com.raisedeveloper.server.domain.exercise.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.exercise.domain.Exercise;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
}
