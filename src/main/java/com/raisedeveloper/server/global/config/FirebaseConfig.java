package com.raisedeveloper.server.global.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

	private final ResourceLoader resourceLoader;

	@Value("${firebase.service-account-key-path:}")
	private String serviceAccountKeyPath;

	@Bean
	public FirebaseApp firebaseApp() {
		if (serviceAccountKeyPath == null || serviceAccountKeyPath.isEmpty()) {
			throw new IllegalStateException(
				"Firebase service account key 경로가 설정되지 않았습니다. "
					+ "application.properties에 firebase.service-account-key-path를 설정하세요."
			);
		}

		try {
			Resource resource = resourceLoader.getResource(serviceAccountKeyPath);
			if (!resource.exists()) {
				throw new IllegalStateException(
					"Firebase service account key 파일을 찾을 수 없습니다: " + serviceAccountKeyPath
				);
			}

			InputStream serviceAccount = resource.getInputStream();

			FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp app = FirebaseApp.initializeApp(options);
				log.info("Firebase 초기화 완료");
				return app;
			}

			return FirebaseApp.getInstance();

		} catch (IOException e) {
			throw new IllegalStateException("Firebase 초기화 실패: " + e.getMessage(), e);
		}
	}

	@Bean
	public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
		return FirebaseMessaging.getInstance(firebaseApp);
	}
}
