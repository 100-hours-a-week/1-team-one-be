package com.raisedeveloper.server.domain.notification.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.notification.application.NotificationService;
import com.raisedeveloper.server.domain.notification.dto.NotificationListResponse;
import com.raisedeveloper.server.domain.notification.dto.NotificationReadRequest;
import com.raisedeveloper.server.domain.notification.dto.NotificationUnreadCountResponse;
import com.raisedeveloper.server.global.response.ApiResponse;
import com.raisedeveloper.server.global.security.currentuser.CurrentUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users/me/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/unread_count")
	public ApiResponse<NotificationUnreadCountResponse> getUnreadCount(@CurrentUser Long userId) {
		return ApiResponse.of(
			"GET_UNREAD_NOTIFICATIONS_SUCCESS",
			notificationService.getUnreadCount(userId)
		);
	}

	@GetMapping
	public ApiResponse<NotificationListResponse> getNotifications(
		@CurrentUser Long userId,
		@RequestParam(value = "limit", required = false) Integer limit,
		@RequestParam(value = "cursor", required = false) String cursor
	) {
		return ApiResponse.of(
			"GET_NOTIFICATIONS_SUCCESS",
			notificationService.getNotifications(userId, limit, cursor)
		);
	}

	@PostMapping
	public ApiResponse<Object> markRead(
		@CurrentUser Long userId,
		@Valid @RequestBody NotificationReadRequest request
	) {
		notificationService.markReadRange(
			userId,
			request.oldestNotificationId(),
			request.latestNotificationId()
		);
		return ApiResponse.of("NOTIFICATION_READ_SUCCESS", java.util.Map.of());
	}
}
