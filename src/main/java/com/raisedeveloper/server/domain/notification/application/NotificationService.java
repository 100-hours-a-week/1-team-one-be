package com.raisedeveloper.server.domain.notification.application;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.notification.domain.UserNotification;
import com.raisedeveloper.server.domain.notification.dto.NotificationItemResponse;
import com.raisedeveloper.server.domain.notification.dto.NotificationListResponse;
import com.raisedeveloper.server.domain.notification.dto.NotificationPagingResponse;
import com.raisedeveloper.server.domain.notification.dto.NotificationUnreadCountResponse;
import com.raisedeveloper.server.domain.notification.infra.UserNotificationRepository;
import com.raisedeveloper.server.domain.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

	private static final int DEFAULT_LIMIT = 20;
	private static final int MAX_LIMIT = 50;

	private final UserNotificationRepository userNotificationRepository;
	private final NotificationCursor notificationCursor;

	public NotificationUnreadCountResponse getUnreadCount(Long userId) {
		long count = userNotificationRepository.countByUserIdAndIsReadFalse(userId);
		return new NotificationUnreadCountResponse(count);
	}

	public NotificationListResponse getNotifications(Long userId, Integer limit, String cursor) {
		int size = normalizeLimit(limit);
		NotificationCursor.Cursor decoded = notificationCursor.decode(cursor);
		List<UserNotification> notifications = fetchNotifications(userId, size, decoded);
		boolean hasNext = notifications.size() > size;
		List<UserNotification> sliced = notifications.stream()
			.limit(size)
			.toList();
		String nextCursor = buildNextCursor(sliced);
		List<NotificationItemResponse> items = sliced.stream()
			.map(this::toItem)
			.toList();
		return new NotificationListResponse(items, new NotificationPagingResponse(nextCursor, hasNext));
	}

	@Transactional
	public void markReadRange(Long userId, Long oldestNotificationId, Long latestNotificationId) {
		long minId = Math.min(oldestNotificationId, latestNotificationId);
		long maxId = Math.max(oldestNotificationId, latestNotificationId);
		userNotificationRepository.markReadBetween(userId, minId, maxId);
	}

	private List<UserNotification> fetchNotifications(Long userId, int size, NotificationCursor.Cursor cursor) {
		PageRequest pageable = PageRequest.of(0, size + 1);
		if (cursor == null) {
			return userNotificationRepository.findPageByUserId(userId, pageable);
		}
		return userNotificationRepository.findPageByUserIdAndCursor(
			userId,
			cursor.createdAt(),
			cursor.id(),
			pageable
		);
	}

	@Transactional
	public void createStretchingSuccess(User user, int earnedExp) {
		String details = String.format("exp +%dp", earnedExp);
		UserNotification notification = new UserNotification(
			user,
			NotificationConstants.STRETCHING_SUCCESS,
			details
		);
		userNotificationRepository.save(notification);
	}

	@Transactional
	public void createStretchingFailed(User user) {
		UserNotification notification = new UserNotification(
			user,
			NotificationConstants.STRETCHING_FAILED,
			null
		);
		userNotificationRepository.save(notification);
	}

	private int normalizeLimit(Integer limit) {
		if (limit == null) {
			return DEFAULT_LIMIT;
		}
		int normalized = Math.max(1, limit);
		return Math.min(MAX_LIMIT, normalized);
	}

	private String buildNextCursor(List<UserNotification> notifications) {
		if (notifications.isEmpty()) {
			return null;
		}
		UserNotification last = notifications.getLast();
		return notificationCursor.encode(last.getCreatedAt(), last.getId());
	}

	private NotificationItemResponse toItem(UserNotification notification) {
		return new NotificationItemResponse(
			notification.getId(),
			notification.getCreatedAt(),
			notification.getContent(),
			notification.getDetails(),
			notification.isRead()
		);
	}
}
