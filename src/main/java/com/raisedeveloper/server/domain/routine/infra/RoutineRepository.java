package com.raisedeveloper.server.domain.routine.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.routine.domain.Routine;

@Repository
public interface RoutineRepository extends JpaRepository<Routine, Long> {
	List<Routine> findAllByUserIdAndIsActiveTrue(Long userId);

	@Query("SELECT r FROM Routine r "
		+ "WHERE r.user.id = :userId "
		+ "AND r.isActive = true "
		+ "ORDER BY r.routineOrder ASC "
		+ "LIMIT 1")
	Optional<Routine> findActiveRoutineByUserId(@Param("userId") Long userId);

	@Query("SELECT r FROM Routine r "
		+ "WHERE r.user.id = :userId "
		+ "AND r.isActive = true "
		+ "ORDER BY CASE WHEN r.lastUsedAt IS NULL THEN 0 ELSE 1 END, "
		+ "r.lastUsedAt ASC, "
		+ "r.routineOrder ASC "
		+ "LIMIT 1")
	Optional<Routine> findLeastRecentlyUsedByUserId(@Param("userId") Long userId);
}
