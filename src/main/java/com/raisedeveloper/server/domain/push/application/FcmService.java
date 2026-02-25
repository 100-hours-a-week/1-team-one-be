package com.raisedeveloper.server.domain.push.application;

import static com.raisedeveloper.server.domain.common.MessageConstants.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
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
	@Async("pushExecutor")
	public void sendSessionPush(User user, ExerciseSession session) {
		log.info("FCM 세션 알림 전송 (비동기) - userId: {}, sessionId: {}, thread: {}",
			user.getId(), session.getId(), Thread.currentThread().getName());

		FcmToken fcmToken = fcmTokenRepository
			.findFirstByUserIdAndRevokedAtNull(user.getId())
			.orElseGet(() -> {
				log.warn("FCM 토큰이 없습니다 - userId: {}", user.getId());
				return null;
			});

		if (fcmToken == null) {
			return;
		}

		try {
			String link = buildSessionLink(session);
			sendMessageToToken(fcmToken.getToken(), buildSessionData(session), link, iconUrl);
			fcmTokenTxService.markTokenUsed(fcmToken);

			log.info("FCM 알림 전송 성공 (비동기) - userId: {}, thread: {}",
				user.getId(), Thread.currentThread().getName());
		} catch (FirebaseMessagingException e) {
			log.error("FCM 알림 전송 실패 - userId: {}, token: {}",
				user.getId(), fcmToken.getToken(), e);
			handleSendFailure(fcmToken, e);
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

	private Map<String, String> buildSessionData(ExerciseSession session) {
		String timestamp = LocalDateTime.now()
			.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

		return Map.of(
			"type", "SESSION_READY",
			"userId", String.valueOf(session.getUser().getId()),
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

	private void handleSendFailure(FcmToken fcmToken, Exception exception) {
		if (exception instanceof FirebaseMessagingException fme) {
			String errorCode = fme.getMessagingErrorCode() != null
				? fme.getMessagingErrorCode().name()
				: "UNKNOWN";

			if ("INVALID_ARGUMENT".equals(errorCode) || "UNREGISTERED".equals(errorCode)) {
				log.warn("유효하지 않은 FCM 토큰 - userId: {}, token: {}, errorCode: {}",
					fcmToken.getUser().getId(), fcmToken.getToken(), errorCode);
				fcmTokenTxService.revokeToken(fcmToken);
			}
		}
	}
}
