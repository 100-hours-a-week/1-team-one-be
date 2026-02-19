package com.raisedeveloper.server.domain.survey.application;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.routine.application.RoutineGenerationJobService;
import com.raisedeveloper.server.domain.routine.client.dto.AiSurveyQuestion;
import com.raisedeveloper.server.domain.routine.domain.RoutineGenerationJob;
import com.raisedeveloper.server.domain.survey.domain.Survey;
import com.raisedeveloper.server.domain.survey.domain.SurveyOption;
import com.raisedeveloper.server.domain.survey.domain.SurveyQuestion;
import com.raisedeveloper.server.domain.survey.domain.SurveyResponse;
import com.raisedeveloper.server.domain.survey.domain.SurveySubmission;
import com.raisedeveloper.server.domain.survey.dto.SurveyDetailResponse;
import com.raisedeveloper.server.domain.survey.dto.SurveyOptionResponse;
import com.raisedeveloper.server.domain.survey.dto.SurveyQuestionResponse;
import com.raisedeveloper.server.domain.survey.dto.SurveySubmissionAnswerRequest;
import com.raisedeveloper.server.domain.survey.dto.SurveySubmissionRequest;
import com.raisedeveloper.server.domain.survey.dto.SurveySubmissionResponse;
import com.raisedeveloper.server.domain.survey.infra.SurveyOptionRepository;
import com.raisedeveloper.server.domain.survey.infra.SurveyQuestionRepository;
import com.raisedeveloper.server.domain.survey.infra.SurveyRepository;
import com.raisedeveloper.server.domain.survey.infra.SurveyResponseRepository;
import com.raisedeveloper.server.domain.survey.infra.SurveySubmissionRepository;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

	private final SurveyRepository surveyRepository;
	private final SurveyQuestionRepository surveyQuestionRepository;
	private final SurveyOptionRepository surveyOptionRepository;
	private final SurveySubmissionRepository surveySubmissionRepository;
	private final SurveyResponseRepository surveyResponseRepository;
	private final UserRepository userRepository;
	private final RoutineGenerationJobService routineGenerationJobService;

	@Cacheable(cacheNames = "surveyDetail")
	public SurveyDetailResponse getSurvey() {
		Survey survey = surveyRepository.findFirstByIsActiveTrueOrderByVersionDesc()
			.orElseThrow(() -> new CustomException(ErrorCode.SURVEY_NOT_FOUND));

		List<SurveyQuestion> questions = surveyQuestionRepository.findAllBySurveyId(survey.getId());
		List<Long> questionIds = questions.stream().map(SurveyQuestion::getId).toList();
		List<SurveyOption> options = surveyOptionRepository.findAllBySurveyQuestionIdIn(
			questionIds);

		Map<Long, List<SurveyOptionResponse>> optionsByQuestionId = options.stream()
			.collect(Collectors.groupingBy(
				option -> option.getSurveyQuestion().getId(),
				Collectors.mapping(
					option -> new SurveyOptionResponse(option.getId(), option.getSortOrder(), option.getContent()),
					Collectors.toList()
				)
			));

		List<SurveyQuestionResponse> questionResponses = questions.stream()
			.map(question -> new SurveyQuestionResponse(
				question.getId(),
				question.getSortOrder(),
				question.getContent(),
				optionsByQuestionId.getOrDefault(question.getId(), List.of())
			))
			.toList();

		return new SurveyDetailResponse(survey.getId(), questionResponses);
	}

	@Transactional
	public SurveySubmissionResponse submitSurvey(Long userId, SurveySubmissionRequest request) {
		User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(
			() -> new CustomException(ErrorCode.USER_NOT_FOUND)
		);
		Survey survey = surveyRepository.findById(request.surveyId()).orElseThrow(
			() -> new CustomException(ErrorCode.SURVEY_NOT_FOUND)
		);

		List<SurveyQuestion> questions = surveyQuestionRepository.findAllBySurveyId(survey.getId());
		Map<Long, SurveyQuestion> questionMap = questions.stream()
			.collect(Collectors.toMap(SurveyQuestion::getId, question -> question));

		List<Long> questionIds = List.copyOf(questionMap.keySet());
		List<SurveyOption> options = surveyOptionRepository
			.findAllBySurveyQuestionIdIn(questionIds);
		Map<Long, Set<Long>> optionIdsByQuestionId = options.stream()
			.collect(Collectors.groupingBy(
				option -> option.getSurveyQuestion().getId(),
				Collectors.mapping(SurveyOption::getId, Collectors.toSet())
			));
		Map<Long, SurveyOption> optionMap = options.stream()
			.collect(Collectors.toMap(SurveyOption::getId, option -> option));

		validateResponses(request.responses(), questionMap, optionIdsByQuestionId);

		SurveySubmission submission = surveySubmissionRepository.save(new SurveySubmission(survey, user));
		List<SurveyResponse> responses = request.responses().stream()
			.map(answer -> new SurveyResponse(
				submission,
				optionMap.get(answer.optionId()),
				questionMap.get(answer.questionId())
			))
			.toList();
		surveyResponseRepository.saveAll(responses);

		List<AiSurveyQuestion> aiSurveyQuestions = request.responses().stream()
			.map(answer -> {
				SurveyQuestion question = questionMap.get(answer.questionId());
				SurveyOption option = optionMap.get(answer.optionId());
				return new AiSurveyQuestion(
					question.getContent(),
					option.getSortOrder()
				);
			})
			.toList();

		RoutineGenerationJob job = routineGenerationJobService.createJobAndRequest(
			user,
			submission,
			aiSurveyQuestions
		);

		return SurveySubmissionResponse.from(submission.getId(), job.getJobId(), job.getStatus());
	}

	private void validateResponses(
		List<SurveySubmissionAnswerRequest> responses,
		Map<Long, SurveyQuestion> questionMap,
		Map<Long, Set<Long>> optionIdsByQuestionId
	) {
		Set<Long> seenQuestions = new HashSet<>();
		Set<Long> seenOptions = new HashSet<>();
		for (SurveySubmissionAnswerRequest response : responses) {
			Long questionId = response.questionId();
			Long optionId = response.optionId();

			if (!questionMap.containsKey(questionId)) {
				throw new CustomException(
					ErrorCode.VALIDATION_FAILED,
					List.of(ErrorDetail.field("responses", "questionId is invalid"))
				);
			}
			if (!seenQuestions.add(questionId)) {
				throw new CustomException(
					ErrorCode.VALIDATION_FAILED,
					List.of(ErrorDetail.field("responses", "duplicate questionId"))
				);
			}
			Set<Long> optionIds = optionIdsByQuestionId.getOrDefault(questionId, Set.of());
			if (!optionIds.contains(optionId)) {
				throw new CustomException(
					ErrorCode.VALIDATION_FAILED,
					List.of(ErrorDetail.field("responses", "optionId is invalid"))
				);
			}
			if (!seenOptions.add(optionId)) {
				throw new CustomException(
					ErrorCode.VALIDATION_FAILED,
					List.of(ErrorDetail.field("responses", "duplicate optionId"))
				);
			}
		}
	}

}
