package com.raisedeveloper.server.domain.user.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;

public interface UserAlarmSettingsRepository extends JpaRepository<UserAlarmSettings, Long> {

	Optional<UserAlarmSettings> findByUserId(Long userId);

	@Query("SELECT uas FROM UserAlarmSettings uas "
		+ "JOIN FETCH uas.user u "
		+ "WHERE u.id IN :userIds")
	List<UserAlarmSettings> findByUserIdInWithUser(@Param("userIds") List<Long> userIds);

	@Query("SELECT uas FROM UserAlarmSettings uas JOIN FETCH uas.user u")
	List<UserAlarmSettings> findAllWithUser();
}
