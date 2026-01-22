package com.raisedeveloper.server.domain.notification.infra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.domain.notification.domain.UserNotification;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

	long countByUserIdAndIsReadFalse(Long userId);

	@Query("""
		select n
		from UserNotification n
		where n.user.id = :userId
		order by n.createdAt desc, n.id desc
		""")
	List<UserNotification> findPageByUserId(
		@Param("userId") Long userId,
		Pageable pageable
	);

	@Query("""
		select n
		from UserNotification n
		where n.user.id = :userId
		  and (n.createdAt < :createdAt or (n.createdAt = :createdAt and n.id < :id))
		order by n.createdAt desc, n.id desc
		""")
	List<UserNotification> findPageByUserIdAndCursor(
		@Param("userId") Long userId,
		@Param("createdAt") LocalDateTime createdAt,
		@Param("id") Long id,
		Pageable pageable
	);

	@Modifying
	@Query("""
		update UserNotification n
		set n.isRead = true
		where n.user.id = :userId
		  and n.createdAt <= :time
		  and n.isRead = false
		""")
	int markReadUpTo(@Param("userId") Long userId, @Param("time") LocalDateTime time);
}
