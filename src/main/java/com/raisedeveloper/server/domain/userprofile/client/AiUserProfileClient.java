package com.raisedeveloper.server.domain.userprofile.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.raisedeveloper.server.domain.userprofile.dto.AiUserProfileSyncRequest;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiUserProfileClient {

	private final RestTemplate aiServerRestTemplate;

	@Value("${ai.server.base-url}")
	private String baseUrl;

	@Value("${ai.server.api.user-profiles-path}")
	private String userProfilesPath;

	public void updateProfiles(AiUserProfileSyncRequest request) {
		String url = baseUrl + userProfilesPath;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<AiUserProfileSyncRequest> entity = new HttpEntity<>(request, headers);

		try {
			aiServerRestTemplate.postForEntity(url, entity, Void.class);
			log.info("AI 사용자 프로필 동기화 완료: profileCount={}", request.profiles().size());
		} catch (ResourceAccessException e) {
			log.error("AI 사용자 프로필 동기화 연결 실패: url={}", url, e);
			throw new CustomException(ErrorCode.AI_SERVER_CONNECTION_FAILED);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("AI 사용자 프로필 동기화 HTTP 오류: statusCode={}, responseBody={}",
				e.getStatusCode(), e.getResponseBodyAsString(), e);

			if (e.getStatusCode().is5xxServerError()) {
				throw new CustomException(ErrorCode.AI_SERVER_ERROR);
			}

			throw new CustomException(ErrorCode.AI_USER_PROFILE_SYNC_FAILED);
		} catch (Exception e) {
			log.error("AI 사용자 프로필 동기화 예상치 못한 오류", e);
			throw new CustomException(ErrorCode.AI_USER_PROFILE_SYNC_FAILED);
		}
	}
}
