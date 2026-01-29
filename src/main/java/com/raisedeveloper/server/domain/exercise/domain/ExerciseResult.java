package com.raisedeveloper.server.domain.exercise.domain;

import java.time.LocalDateTime;

import com.raisedeveloper.server.domain.common.domain.CreatedUpdatedEntity;
import com.raisedeveloper.server.domain.exercise.enums.ExerciseResultStatus;
import com.raisedeveloper.server.domain.routine.domain.RoutineStep;

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
@Table(name = "exercise_results")
public class ExerciseResult extends CreatedUpdatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercise_session_id", nullable = false)
	private ExerciseSession exerciseSession;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "routine_step_id", nullable = false)
	private RoutineStep routineStep;

	@Column(nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private ExerciseResultStatus status;

	private byte accuracy;

	private String poseRecord;

	private LocalDateTime startAt;

	private LocalDateTime endAt;

	public ExerciseResult(
		ExerciseSession exerciseSession,
		RoutineStep routineStep
	) {
		this.exerciseSession = exerciseSession;
		this.routineStep = routineStep;
		this.status = ExerciseResultStatus.PENDING;
	}

	public void stepResultsUpdate(ExerciseResultStatus status, byte accuracy, String poseRecord, LocalDateTime startAt,
		LocalDateTime endAt) {
		this.status = status;
		this.accuracy = accuracy;
		this.poseRecord = poseRecord;
		this.startAt = startAt;
		this.endAt = endAt;
	}

	public void stepResultsSkipped() {
		this.status = ExerciseResultStatus.SKIPPED;
	}

	public void stepResultsFailed(LocalDateTime failedAt) {
		this.status = ExerciseResultStatus.FAILED;
		if (this.endAt == null) {
			this.endAt = failedAt;
		}
	}
}
