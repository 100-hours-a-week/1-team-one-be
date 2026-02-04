package com.raisedeveloper.server.domain.push.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.raisedeveloper.server.domain.auth.domain.FcmToken;
import com.raisedeveloper.server.domain.auth.infra.FcmTokenRepository;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FcmService implements PushService {

	private final FirebaseMessaging firebaseMessaging;
	private final FcmTokenRepository fcmTokenRepository;

	@Override
	@Async("pushExecutor")
	@Transactional(propagation = Propagation.REQUIRES_NEW)  // ✅ 독립 트랜잭션
	public void sendSessionPush(User user, ExerciseSession session) {
		String title = "운동할 시간이에요";
		String body = "오늘 루틴을 시작해볼까요?";

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
			sendMessageToToken(fcmToken.getToken(), title, body, buildSessionData(session));
			fcmToken.used();
			fcmTokenRepository.save(fcmToken);

			log.info("FCM 알림 전송 성공 (비동기) - userId: {}, thread: {}",
				user.getId(), Thread.currentThread().getName());
		} catch (FirebaseMessagingException e) {
			log.error("FCM 알림 전송 실패 - userId: {}, token: {}",
				user.getId(), fcmToken.getToken(), e);
			handleSendFailure(fcmToken, e);
		}
	}

	private void sendMessageToToken(String token, String title, String body, Map<String, String> data)
		throws FirebaseMessagingException {

		if (firebaseMessaging == null) {
			log.warn("Firebase Messaging이 초기화되지 않았습니다. 알림을 전송할 수 없습니다.");
			return;
		}

		Message.Builder messageBuilder = Message.builder()
			.setToken(token)
			.setNotification(Notification.builder()
				.setTitle(title)
				.setBody(body)
				.build());

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

	private void handleSendFailure(FcmToken fcmToken, Exception exception) {
		if (exception instanceof FirebaseMessagingException fme) {
			String errorCode = fme.getMessagingErrorCode() != null
				? fme.getMessagingErrorCode().name()
				: "UNKNOWN";

			if ("INVALID_ARGUMENT".equals(errorCode) || "UNREGISTERED".equals(errorCode)) {
				log.warn("유효하지 않은 FCM 토큰 - userId: {}, token: {}, errorCode: {}",
					fcmToken.getUser().getId(), fcmToken.getToken(), errorCode);
				fcmToken.revoke();
				fcmTokenRepository.save(fcmToken);
			}
		}
	}
}
