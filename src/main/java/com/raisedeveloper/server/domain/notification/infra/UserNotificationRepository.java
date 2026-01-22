package com.raisedeveloper.server.domain.notification.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raisedeveloper.server.domain.notification.domain.UserNotification;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

}
