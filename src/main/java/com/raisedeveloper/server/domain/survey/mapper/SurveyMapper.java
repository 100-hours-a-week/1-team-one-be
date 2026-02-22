package com.raisedeveloper.server.domain.survey.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.common.enums.RoutineGenerationJobStatus;
import com.raisedeveloper.server.domain.survey.domain.Survey;
import com.raisedeveloper.server.domain.survey.domain.SurveyOption;
import com.raisedeveloper.server.domain.survey.domain.SurveyQuestion;
import com.raisedeveloper.server.domain.survey.dto.SurveyDetailResponse;
import com.raisedeveloper.server.domain.survey.dto.SurveyOptionResponse;
import com.raisedeveloper.server.domain.survey.dto.SurveyQuestionResponse;
import com.raisedeveloper.server.domain.survey.dto.SurveySubmissionResponse;

@Component
public class SurveyMapper {

	public SurveyDetailResponse toSurveyDetailResponse(
		Survey survey,
		List<SurveyQuestion> questions,
		List<SurveyOption> options
	) {
		Map<Long, List<SurveyOptionResponse>> optionsByQuestionId = groupOptionsByQuestionId(options);
		List<SurveyQuestionResponse> questionResponses = toQuestionResponses(questions, optionsByQuestionId);
		return new SurveyDetailResponse(survey.getId(), questionResponses);
	}

	public Map<Long, List<SurveyOptionResponse>> groupOptionsByQuestionId(List<SurveyOption> options) {
		return options.stream()
			.collect(Collectors.groupingBy(
				option -> option.getSurveyQuestion().getId(),
				Collectors.mapping(
					this::toOptionResponse,
					Collectors.toList()
				)
			));
	}

	public List<SurveyQuestionResponse> toQuestionResponses(
		List<SurveyQuestion> questions,
		Map<Long, List<SurveyOptionResponse>> optionsByQuestionId
	) {
		return questions.stream()
			.map(question -> new SurveyQuestionResponse(
				question.getId(),
				question.getSortOrder(),
				question.getContent(),
				optionsByQuestionId.getOrDefault(question.getId(), List.of())
			))
			.toList();
	}

	public SurveyOptionResponse toOptionResponse(SurveyOption option) {
		return new SurveyOptionResponse(option.getId(), option.getSortOrder(), option.getContent());
	}

	public SurveySubmissionResponse toSubmissionResponse(
		Long submissionId,
		String jobId,
		RoutineGenerationJobStatus status
	) {
		return new SurveySubmissionResponse(submissionId, jobId, status);
	}
}
