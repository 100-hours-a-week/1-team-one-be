package com.raisedeveloper.server.domain.user.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raisedeveloper.server.domain.user.domain.UserAlarmSettings;

public interface UserAlarmSettingsRepository extends JpaRepository<UserAlarmSettings, Long> {

	Optional<UserAlarmSettings> findByUserId(Long userId);
}
