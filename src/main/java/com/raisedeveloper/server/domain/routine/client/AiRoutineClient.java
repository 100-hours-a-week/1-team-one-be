package com.raisedeveloper.server.domain.routine.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.raisedeveloper.server.domain.routine.client.dto.AiRoutineAsyncRequest;
import com.raisedeveloper.server.domain.routine.client.dto.AiRoutineRequest;
import com.raisedeveloper.server.domain.routine.client.dto.AiRoutineResponse;
import com.raisedeveloper.server.domain.routine.client.dto.AiSurveyData;
import com.raisedeveloper.server.domain.routine.client.dto.AiSurveyQuestion;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiRoutineClient {

	private final RestTemplate aiServerRestTemplate;

	@Value("${ai.server.base-url}")
	private String baseUrl;

	@Value("${ai.server.api.routines-path}")
	private String routinesPath;

	@Value("${ai.server.api.routines-async-path}")
	private String routinesAsyncPath;

	@Value("${ai.server.routine-count}")
	private int routineCount;

	public AiRoutineResponse generateRoutines(
		Long userId,
		Long surveySubmissionId,
		List<AiSurveyQuestion> aiSurveyQuestions
	) {
		String url = baseUrl + routinesPath;

		AiRoutineRequest request = buildRequest(aiSurveyQuestions);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<AiRoutineRequest> entity = new HttpEntity<>(request, headers);

		try {
			log.info("AI 서버 루틴 생성 요청 시작: userId={}, surveySubmissionId={}, url={}",
				userId, surveySubmissionId, url);

			ResponseEntity<AiRoutineResponse> response = aiServerRestTemplate.postForEntity(
				url,
				entity,
				AiRoutineResponse.class
			);

			AiRoutineResponse aiResponse = response.getBody();

			log.info("AI 서버 루틴 생성 응답 수신: status={}, routineCount={}",
				aiResponse != null ? aiResponse.status() : null,
				aiResponse != null && aiResponse.routines() != null ? aiResponse.routines().size() : 0
			);

			return aiResponse;

		} catch (HttpMessageNotReadableException e) {
			log.error("AI 서버 응답 파싱 실패: url={}, message={}",
				url, e.getMostSpecificCause().getMessage(), e);
			throw new CustomException(
				ErrorCode.AI_RESPONSE_PARSE_FAILED,
				List.of(ErrorDetail.field("parseError",
					e.getMostSpecificCause().getMessage()))
			);
		} catch (ResourceAccessException e) {
			log.error("AI 서버 연결 실패: url={}", url, e);
			throw new CustomException(ErrorCode.AI_SERVER_CONNECTION_FAILED);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("AI 서버 HTTP 오류: statusCode={}, responseBody={}",
				e.getStatusCode(), e.getResponseBodyAsString(), e);

			if (e.getStatusCode().is5xxServerError()) {
				throw new CustomException(ErrorCode.AI_SERVER_ERROR);
			}

			throw new CustomException(
				ErrorCode.AI_ROUTINE_GENERATION_FAILED,
				List.of(ErrorDetail.field("aiServerError", e.getStatusCode().toString()))
			);

		} catch (Exception e) {
			log.error("AI 서버 호출 중 예상치 못한 오류 발생", e);
			throw new CustomException(ErrorCode.AI_ROUTINE_GENERATION_FAILED);
		}
	}

	public void requestRoutineGenerationAsync(
		AiRoutineAsyncRequest request
	) {
		String url = baseUrl + routinesAsyncPath;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<AiRoutineAsyncRequest> entity = new HttpEntity<>(request, headers);

		try {
			log.info("AI 서버 비동기 루틴 생성 요청 시작: taskId={}, url={}",
				request.taskId(), url);

			aiServerRestTemplate.postForEntity(url, entity, Void.class);

			log.info("AI 서버 비동기 루틴 생성 요청 완료: taskId={}", request.taskId());

		} catch (ResourceAccessException e) {
			log.error("AI 서버 연결 실패: url={}", url, e);
			throw new CustomException(ErrorCode.AI_SERVER_CONNECTION_FAILED);

		} catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("AI 서버 HTTP 오류: statusCode={}, responseBody={}",
				e.getStatusCode(), e.getResponseBodyAsString(), e);

			if (e.getStatusCode().is5xxServerError()) {
				throw new CustomException(ErrorCode.AI_SERVER_ERROR);
			}

			throw new CustomException(
				ErrorCode.AI_ROUTINE_GENERATION_FAILED,
				List.of(ErrorDetail.field("aiServerError", e.getStatusCode().toString()))
			);

		} catch (Exception e) {
			log.error("AI 서버 비동기 요청 중 예상치 못한 오류 발생", e);
			throw new CustomException(ErrorCode.AI_ROUTINE_GENERATION_FAILED);
		}
	}

	private AiRoutineRequest buildRequest(
		List<AiSurveyQuestion> aiSurveyQuestions
	) {
		AiSurveyData surveyData = new AiSurveyData(
			routineCount,
			aiSurveyQuestions
		);

		return new AiRoutineRequest(
			surveyData
		);
	}

	public AiRoutineAsyncRequest buildAsyncRequest(
		String taskId,
		Long userId,
		List<AiSurveyQuestion> aiSurveyQuestions
	) {
		AiSurveyData surveyData = new AiSurveyData(
			routineCount,
			aiSurveyQuestions
		);

		return new AiRoutineAsyncRequest(
			taskId,
			userId,
			surveyData
		);
	}
}
