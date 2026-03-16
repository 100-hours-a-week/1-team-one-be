package com.raisedeveloper.server.domain.satisfaction.infra;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.satisfaction.domain.ExerciseSatisfaction;

@Repository
public interface ExerciseSatisfactionRepository extends JpaRepository<ExerciseSatisfaction, Long> {

	List<ExerciseSatisfaction> findAllByUserIdAndExerciseIdIn(Long userId, Collection<Long> exerciseIds);

	@Query("""
		SELECT
			es.user.id AS userId,
			es.exercise.id AS exerciseId,
			es.satisfaction AS satisfaction
		FROM ExerciseSatisfaction es
		ORDER BY es.id ASC
		""")
	List<ExerciseSatisfactionSyncProjection> findAllForAiSync();

	interface ExerciseSatisfactionSyncProjection {
		Long getUserId();

		Long getExerciseId();

		Byte getSatisfaction();
	}
}
