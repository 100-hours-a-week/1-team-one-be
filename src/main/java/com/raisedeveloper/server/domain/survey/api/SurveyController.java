package com.raisedeveloper.server.domain.survey.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.survey.application.SurveyService;
import com.raisedeveloper.server.domain.survey.dto.SurveyDetailResponse;
import com.raisedeveloper.server.domain.survey.dto.SurveySubmissionRequest;
import com.raisedeveloper.server.domain.survey.dto.SurveySubmissionResponse;
import com.raisedeveloper.server.global.response.ApiResponse;
import com.raisedeveloper.server.global.security.utils.AuthUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SurveyController {

	private final SurveyService surveyService;

	@GetMapping("/survey")
	public ApiResponse<SurveyDetailResponse> getSurvey() {
		AuthUtils.resolveUserIdFromContext();
		return ApiResponse.success("GET_SURVEY_SUCCESS", surveyService.getSurvey());
	}

	@PostMapping("/survey-submission")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ApiResponse<SurveySubmissionResponse> submitSurvey(
		@Valid @RequestBody SurveySubmissionRequest request
	) {
		Long userId = AuthUtils.resolveUserIdFromContext();
		return ApiResponse.success(
			"CREATE_SURVEY_SUBMISSION_SUCCESS",
			surveyService.submitSurvey(userId, request)
		);
	}
}
