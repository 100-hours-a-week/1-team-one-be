package com.raisedeveloper.server.domain.routine.domain;

import com.raisedeveloper.server.domain.common.domain.CreatedAtEntity;
import com.raisedeveloper.server.domain.common.enums.RoutineStatus;
import com.raisedeveloper.server.domain.survey.domain.SurveySubmission;
import com.raisedeveloper.server.domain.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "routines")
public class Routine extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_submission_id", nullable = false)
	private SurveySubmission surveySubmission;

	@Column(nullable = false)
	private short routineOrder;

	@Column(nullable = false)
	private boolean isActive;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private RoutineStatus status;

	@Column(nullable = false)
	private String reason;

	public Routine(
		User user,
		SurveySubmission surveySubmission,
		short routineOrder,
		RoutineStatus status,
		String reason
	) {
		this.user = user;
		this.surveySubmission = surveySubmission;
		this.routineOrder = routineOrder;
		this.isActive = true;
		this.status = status;
		this.reason = reason;
	}

	public void routineInactivated() {
		this.isActive = false;
	}
}
