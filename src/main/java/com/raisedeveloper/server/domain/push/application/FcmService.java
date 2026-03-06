package com.raisedeveloper.server.domain.push.application;

import static com.raisedeveloper.server.domain.common.MessageConstants.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushFcmOptions;
import com.google.firebase.messaging.WebpushNotification;
import com.raisedeveloper.server.domain.auth.domain.FcmToken;
import com.raisedeveloper.server.domain.auth.infra.FcmTokenRepository;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService implements PushService {

	private final FirebaseMessaging firebaseMessaging;
	private final FcmTokenRepository fcmTokenRepository;
	private final FcmTokenTxService fcmTokenTxService;

	@Value("${server.base-url}")
	private String serverBaseUrl;

	@Value("${icon.url}")
	private String iconUrl;

	@Override
	public PushDeliveryStatus sendSessionPush(User user, ExerciseSession session) {
		FcmToken fcmToken = fcmTokenRepository
			.findFirstByUserIdAndRevokedAtNull(user.getId())
			.orElseGet(() -> {
				log.warn("FCM 토큰이 없습니다 - userId: {}", user.getId());
				return null;
			});

		if (fcmToken == null) {
			return PushDeliveryStatus.FAILED_PERMANENT;
		}

		try {
			String link = buildSessionLink(session);
			sendMessageToToken(fcmToken.getToken(), buildSessionData(user.getId(), session), link, iconUrl);
			fcmTokenTxService.markTokenUsed(fcmToken);

			return PushDeliveryStatus.SENT;
		} catch (FirebaseMessagingException e) {
			log.error("FCM 알림 전송 실패 - userId: {}, sessionId: {}, token: {}",
				user.getId(), session.getId(), fcmToken.getToken(), e);
			if (handleSendFailure(fcmToken, e)) {
				return PushDeliveryStatus.FAILED_PERMANENT;
			}
			throw new IllegalStateException("Retryable FCM failure: " + e.getMessage(), e);
		}
	}

	private void sendMessageToToken(
		String token,
		Map<String, String> data,
		String link,
		String iconUrl
	)
		throws FirebaseMessagingException {

		if (firebaseMessaging == null) {
			log.warn("Firebase Messaging이 초기화되지 않았습니다. 알림을 전송할 수 없습니다.");
			return;
		}

		Message.Builder messageBuilder = Message.builder()
			.setToken(token)
			.setNotification(Notification.builder()
				.setTitle(SESSION_TITLE)
				.setBody(SESSION_BODY)
				.build());

		messageBuilder.setWebpushConfig(
			WebpushConfig.builder()
				.setFcmOptions(WebpushFcmOptions.builder().setLink(link).build())
				.setNotification(WebpushNotification.builder().setIcon(iconUrl).build())
				.build()
		);

		if (data != null && !data.isEmpty()) {
			messageBuilder.putAllData(data);
		}

		String response = firebaseMessaging.send(messageBuilder.build());
		log.info("FCM 알림 전송 성공 - token: {}, response: {}", token, response);
	}

	private Map<String, String> buildSessionData(Long userId, ExerciseSession session) {
		String timestamp = LocalDateTime.now()
			.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

		return Map.of(
			"type", "SESSION_READY",
			"userId", String.valueOf(userId),
			"ts", timestamp,
			"sessionId", String.valueOf(session.getId()),
			"routineId", String.valueOf(session.getRoutine().getId())
		);
	}

	private String buildSessionLink(ExerciseSession session) {
		String normalizedBase = serverBaseUrl.endsWith("/") ? serverBaseUrl.substring(0, serverBaseUrl.length() - 1)
			: serverBaseUrl;
		return normalizedBase + "/stretch/" + session.getId();
	}

	private boolean handleSendFailure(FcmToken fcmToken, FirebaseMessagingException exception) {
		String errorCode = exception.getMessagingErrorCode() != null
			? exception.getMessagingErrorCode().name()
			: "UNKNOWN";

		if ("INVALID_ARGUMENT".equals(errorCode) || "UNREGISTERED".equals(errorCode)) {
			log.warn("유효하지 않은 FCM 토큰 - userId: {}, token: {}, errorCode: {}",
				fcmToken.getUser().getId(), fcmToken.getToken(), errorCode);
			fcmTokenTxService.revokeToken(fcmToken);
			return true;
		}
		return false;
	}
}
