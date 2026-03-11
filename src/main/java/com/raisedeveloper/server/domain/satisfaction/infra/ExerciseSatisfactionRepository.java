package com.raisedeveloper.server.domain.satisfaction.infra;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.satisfaction.domain.ExerciseSatisfaction;

@Repository
public interface ExerciseSatisfactionRepository extends JpaRepository<ExerciseSatisfaction, Long> {

	List<ExerciseSatisfaction> findAllByUserIdAndExerciseIdIn(Long userId, Collection<Long> exerciseIds);
}
