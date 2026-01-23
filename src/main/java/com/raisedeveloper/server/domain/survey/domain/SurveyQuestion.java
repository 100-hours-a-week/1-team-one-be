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
@Table(name = "survey_questions")
public class SurveyQuestion extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_id", nullable = false)
	private Survey survey;

	@Column(nullable = false)
	private short sortOrder;

	@Column(nullable = false, length = 500)
	private String content;

	public SurveyQuestion(Survey survey, short sortOrder, String content) {
		this.survey = survey;
		this.sortOrder = sortOrder;
		this.content = content;
	}
}
