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
		+ "AND uas.repeatDays LIKE %:currentDay% "
		+ "AND (uas.dnd = false OR uas.dndFinishedAt < :now)")
	List<UserAlarmSettings> findActiveAlarmSettings(
		@Param("currentTime") LocalTime currentTime,
		@Param("currentDay") String currentDay,
		@Param("now") LocalDateTime now
	);
}
