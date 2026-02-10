package com.raisedeveloper.server.domain.routine.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raisedeveloper.server.domain.common.enums.RoutineGenerationJobStatus;
import com.raisedeveloper.server.domain.routine.client.AiRoutineClient;
import com.raisedeveloper.server.domain.routine.client.dto.AiRoutineAsyncRequest;
import com.raisedeveloper.server.domain.routine.client.dto.AiRoutineResponse;
import com.raisedeveloper.server.domain.routine.client.dto.AiSurveyQuestion;
import com.raisedeveloper.server.domain.routine.domain.RoutineGenerationJob;
import com.raisedeveloper.server.domain.routine.dto.AiRoutineCallbackRequest;
import com.raisedeveloper.server.domain.routine.infra.RoutineGenerationJobRepository;
import com.raisedeveloper.server.domain.survey.domain.SurveySubmission;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutineGenerationJobService {

	private final RoutineGenerationJobRepository jobRepository;
	private final RoutineService routineService;
	private final ObjectMapper objectMapper;
	private final AiRoutineClient aiRoutineClient;

	@Transactional
	public RoutineGenerationJob createJobAndRequest(
		User user,
		SurveySubmission submission,
		List<AiSurveyQuestion> aiSurveyQuestions
	) {
		String jobId = generateJobId();
		AiRoutineAsyncRequest request = aiRoutineClient.buildAsyncRequest(
			jobId,
			user.getId(),
			aiSurveyQuestions
		);
		String payload = writePayload(request);

		RoutineGenerationJob job = new RoutineGenerationJob(
			jobId,
			user,
			submission,
			RoutineGenerationJobStatus.PENDING,
			payload
		);
		jobRepository.save(job);
		try {
			aiRoutineClient.requestRoutineGenerationAsync(request);
			job.markRequested(LocalDateTime.now());
		} catch (CustomException e) {
			job.markFailed(LocalDateTime.now(), e.getErrorCode().getReason(), null);
		} catch (Exception e) {
			job.markFailed(LocalDateTime.now(), e.getMessage(), null);
		}
		return job;
	}

	@Transactional
	public void handleCallback(AiRoutineCallbackRequest request) {
		Optional<RoutineGenerationJob> jobOpt = jobRepository.findByJobId(request.taskId());
		if (jobOpt.isEmpty()) {
			log.warn("RoutineGenerationJob not found for callback: taskId={}", request.taskId());
			return;
		}

		RoutineGenerationJob job = jobOpt.get();
		if (job.getStatus() == RoutineGenerationJobStatus.COMPLETED
			|| job.getStatus() == RoutineGenerationJobStatus.FAILED) {
			return;
		}

		job.markCallbackReceived(LocalDateTime.now());

		if (request.status() == RoutineGenerationJobStatus.COMPLETED) {
			AiRoutineResponse aiResponse = new AiRoutineResponse(
				RoutineGenerationJobStatus.COMPLETED,
				request.routines()
			);
			try {
				routineService.createRoutinesFromAiResponse(
					job.getUser(),
					job.getSurveySubmission(),
					aiResponse
				);
				job.markCompleted(LocalDateTime.now(), writePayload(aiResponse));
			} catch (CustomException e) {
				job.markFailed(LocalDateTime.now(), e.getErrorCode().getReason(), writePayload(aiResponse));
				return;
			} catch (Exception e) {
				log.error("Routine generation callback processing failed: taskId={}", request.taskId(), e);
				throw new CustomException(ErrorCode.AI_ROUTINE_GENERATION_FAILED);
			}
			return;
		}

		if (request.status() == RoutineGenerationJobStatus.FAILED) {
			job.markFailed(LocalDateTime.now(), request.errorMessage(), writePayload(request));
			return;
		}

		job.markFailed(LocalDateTime.now(), "UNKNOWN_CALLBACK_STATUS", writePayload(request));
	}

	private String writePayload(Object payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new CustomException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
		}
	}

	private String generateJobId() {
		return "job_" + UUID.randomUUID().toString().replace("-", "");
	}
}
