package com.raisedeveloper.server.domain.routine.domain;

import java.time.LocalDateTime;

import com.raisedeveloper.server.domain.common.domain.CreatedUpdatedEntity;
import com.raisedeveloper.server.domain.common.enums.RoutineGenerationJobStatus;
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
@Table(name = "routine_generation_jobs")
public class RoutineGenerationJob extends CreatedUpdatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "job_id", nullable = false, length = 64)
	private String jobId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "survey_submission_id", nullable = false)
	private SurveySubmission surveySubmission;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private RoutineGenerationJobStatus status;

	@Column(nullable = false)
	private LocalDateTime requestedAt;

	private LocalDateTime callbackReceivedAt;

	private LocalDateTime completedAt;

	private LocalDateTime failedAt;

	@Column(columnDefinition = "TEXT")
	private String aiRequestPayload;

	@Column(columnDefinition = "TEXT")
	private String aiResponsePayload;

	@Column(nullable = false)
	private String errorMessage;

	public RoutineGenerationJob(
		String jobId,
		User user,
		SurveySubmission surveySubmission,
		RoutineGenerationJobStatus status,
		String aiRequestPayload
	) {
		this.jobId = jobId;
		this.user = user;
		this.surveySubmission = surveySubmission;
		this.status = status;
		this.aiRequestPayload = aiRequestPayload;
	}

	public void markRequested(LocalDateTime now) {
		this.status = RoutineGenerationJobStatus.REQUESTED;
		this.requestedAt = now;
	}

	public void markCallbackReceived(LocalDateTime now) {
		this.callbackReceivedAt = now;
	}

	public void markCompleted(LocalDateTime now, String aiResponsePayload) {
		this.status = RoutineGenerationJobStatus.COMPLETED;
		this.completedAt = now;
		this.aiResponsePayload = aiResponsePayload;
	}

	public void markFailed(LocalDateTime now, String errorMessage, String aiResponsePayload) {
		this.status = RoutineGenerationJobStatus.FAILED;
		this.failedAt = now;
		this.errorMessage = errorMessage;
		this.aiResponsePayload = aiResponsePayload;
	}
}
