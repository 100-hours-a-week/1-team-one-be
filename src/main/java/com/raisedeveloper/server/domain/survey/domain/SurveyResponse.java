package com.raisedeveloper.server.domain.survey.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "survey_responses")
public class SurveyResponse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_submission_id", nullable = false)
	private SurveySubmission surveySubmission;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_option_id", nullable = false)
	private SurveyOption surveyOption;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_question_id", nullable = false)
	private SurveyQuestion surveyQuestion;

	public SurveyResponse(
		SurveySubmission surveySubmission,
		SurveyOption surveyOption,
		SurveyQuestion surveyQuestion
	) {
		this.surveySubmission = surveySubmission;
		this.surveyOption = surveyOption;
		this.surveyQuestion = surveyQuestion;
	}
}
