package com.raisedeveloper.server.domain.user.infra;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;

public interface UserAlarmSettingsRepository extends JpaRepository<UserAlarmSettings, Long> {

	Optional<UserAlarmSettings> findByUserId(Long userId);

	@Query("SELECT uas FROM UserAlarmSettings uas "
		+ "WHERE uas.activeStartAt <= :currentTime "
		+ "AND uas.activeEndAt >= :currentTime "
		+ "AND uas.repeatDays LIKE %:currentDay% ")
	List<UserAlarmSettings> findActiveAlarmSettings(
		@Param("currentTime") LocalTime currentTime,
		@Param("currentDay") String currentDay
	);

	@Query("SELECT uas FROM UserAlarmSettings uas "
		+ "JOIN FETCH uas.user u "
		+ "WHERE uas.activeStartAt <= :currentTime "
		+ "AND uas.activeEndAt >= :currentTime "
		+ "AND uas.repeatDays LIKE CONCAT('%', :currentDay, '%') "
		+ "AND (uas.focusStartAt IS NULL "
		+ "  OR uas.focusEndAt IS NULL "
		+ "  OR NOT ("
		+ "    CASE WHEN uas.focusStartAt <= uas.focusEndAt "
		+ "      THEN :currentTime >= uas.focusStartAt AND :currentTime <= uas.focusEndAt "
		+ "      ELSE :currentTime >= uas.focusStartAt OR :currentTime <= uas.focusEndAt "
		+ "    END"
		+ "  )"
		+ ") "
		+ "AND (uas.dnd = false "
		+ "  OR uas.dndFinishedAt IS NULL "
		+ "  OR uas.dndFinishedAt <= :now) ")
	List<UserAlarmSettings> findEligibleAlarmSettings(
		@Param("currentTime") LocalTime currentTime,
		@Param("currentDay") String currentDay,
		@Param("now") LocalDateTime now
	);
}
