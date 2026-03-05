package com.raisedeveloper.server.domain.exercise.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSessionReport;

public interface ExerciseSessionReportRepository extends JpaRepository<ExerciseSessionReport, Long> {

	boolean existsByExerciseSessionId(Long exerciseSessionId);

	List<ExerciseSessionReport> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

	@Query("""
		SELECT esr FROM ExerciseSessionReport esr
		JOIN FETCH esr.exerciseSession es
		WHERE esr.id = :reportId AND esr.user.id = :userId
		""")
	Optional<ExerciseSessionReport> findByIdAndUserIdWithSession(
		@Param("reportId") Long reportId,
		@Param("userId") Long userId
	);
}
