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
			log.error(
				"AI routine async request failed: jobId={}, userId={}, errorCode={}, errors={}",
				jobId,
				user.getId(),
				e.getErrorCode(),
				e.getErrors(),
				e
			);
			job.markFailed(LocalDateTime.now(), e.getErrorCode().getReason(), null);
		} catch (Exception e) {
			log.error(
				"AI routine async request failed with unexpected error: jobId={}, userId={}",
				jobId,
				user.getId(),
				e
			);
			job.markFailed(LocalDateTime.now(), e.getMessage(), null);
		}
		return job;
	}

	@Transactional
	public void handleCallback(AiRoutineCallbackRequest request) {
		log.info(
			"Received AI routine callback: taskId={}, userId={}, status={}, errorMessage={}",
			request.taskId(),
			request.userId(),
			request.status(),
			request.errorMessage()
		);

		Optional<RoutineGenerationJob> jobOpt = jobRepository.findByJobId(request.taskId());
		if (jobOpt.isEmpty()) {
			log.warn("RoutineGenerationJob not found for callback: taskId={}, payload={}", request.taskId(), request);
			return;
		}

		RoutineGenerationJob job = jobOpt.get();
		if (job.getStatus() == RoutineGenerationJobStatus.COMPLETED
			|| job.getStatus() == RoutineGenerationJobStatus.FAILED) {
			log.info(
				"Ignoring callback for finalized job: taskId={}, currentStatus={}, callbackStatus={}",
				request.taskId(),
				job.getStatus(),
				request.status()
			);
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
				log.error(
					"Routine callback business validation failed: taskId={}, jobId={}, errorCode={}, errors={}, payload={}",
					request.taskId(),
					job.getJobId(),
					e.getErrorCode(),
					e.getErrors(),
					request,
					e
				);
				job.markFailed(LocalDateTime.now(), e.getErrorCode().getReason(), writePayload(aiResponse));
				return;
			} catch (Exception e) {
				log.error(
					"Routine generation callback processing failed: taskId={}, jobId={}, payload={}",
					request.taskId(),
					job.getJobId(),
					request,
					e
				);
				throw new CustomException(ErrorCode.AI_ROUTINE_GENERATION_FAILED);
			}
			return;
		}

		if (request.status() == RoutineGenerationJobStatus.FAILED) {
			log.warn(
				"AI routine callback reported failure: taskId={}, jobId={}, errorMessage={}, payload={}",
				request.taskId(),
				job.getJobId(),
				request.errorMessage(),
				request
			);
			job.markFailed(LocalDateTime.now(), request.errorMessage(), writePayload(request));
			return;
		}

		log.warn(
			"AI routine callback reported unknown status: taskId={}, jobId={}, status={}, payload={}",
			request.taskId(),
			job.getJobId(),
			request.status(),
			request
		);
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
