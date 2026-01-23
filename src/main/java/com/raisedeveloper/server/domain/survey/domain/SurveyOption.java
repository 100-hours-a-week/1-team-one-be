package com.raisedeveloper.server.domain.survey.domain;

import com.raisedeveloper.server.domain.common.domain.CreatedAtEntity;

import jakarta.persistence.Column;
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
@Table(name = "survey_options")
public class SurveyOption extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_question_id", nullable = false)
	private SurveyQuestion surveyQuestion;

	@Column(nullable = false)
	private byte sortOrder;

	@Column(nullable = false, length = 500)
	private String content;

	public SurveyOption(SurveyQuestion surveyQuestion, byte sortOrder, String content) {
		this.surveyQuestion = surveyQuestion;
		this.sortOrder = sortOrder;
		this.content = content;
	}
}
