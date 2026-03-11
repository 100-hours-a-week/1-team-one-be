package com.raisedeveloper.server.domain.satisfaction.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.raisedeveloper.server.domain.satisfaction.dto.AiExerciseSatisfactionSyncRequest;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiSatisfactionClient {

	private final RestTemplate aiServerRestTemplate;

	@Value("${ai.server.base-url}")
	private String baseUrl;

	@Value("${ai.server.api.satisfaction-path}")
	private String satisfactionPath;

	public void updateSatisfactions(AiExerciseSatisfactionSyncRequest request) {
		String url = baseUrl + satisfactionPath;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<AiExerciseSatisfactionSyncRequest> entity = new HttpEntity<>(request, headers);

		try {
			aiServerRestTemplate.postForEntity(url, entity, Void.class);
			log.info("AI 만족도 동기화 완료: satisfactionCount={}", request.records().size());
		} catch (ResourceAccessException e) {
			log.error("AI 만족도 동기화 연결 실패: url={}", url, e);
			throw new CustomException(ErrorCode.AI_SERVER_CONNECTION_FAILED);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("AI 만족도 동기화 HTTP 오류: statusCode={}, responseBody={}",
				e.getStatusCode(), e.getResponseBodyAsString(), e);

			if (e.getStatusCode().is5xxServerError()) {
				throw new CustomException(ErrorCode.AI_SERVER_ERROR);
			}

			throw new CustomException(ErrorCode.AI_SATISFACTION_SYNC_FAILED);
		} catch (Exception e) {
			log.error("AI 만족도 동기화 예상치 못한 오류", e);
			throw new CustomException(ErrorCode.AI_SATISFACTION_SYNC_FAILED);
		}
	}
}
