package com.raisedeveloper.server.domain.routine.domain;

import com.raisedeveloper.server.domain.common.domain.CreatedAtEntity;
import com.raisedeveloper.server.domain.exercise.domain.Exercise;

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
@Table(name = "routine_steps")
public class RoutineStep extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "routine_id", nullable = false)
	private Routine routine;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercise_id", nullable = false)
	private Exercise exercise;

	@Column(nullable = false)
	private String reason;

	private Short targetReps;

	private Short durationTime;

	@Column(nullable = false)
	private short limitTime;

	@Column(nullable = false)
	private short stepOrder;

	public RoutineStep(
		Routine routine,
		Exercise exercise,
		String reason,
		Short targetReps,
		Short durationTime,
		short limitTime,
		short stepOrder
	) {
		this.routine = routine;
		this.exercise = exercise;
		this.reason = reason;
		this.targetReps = targetReps;
		this.durationTime = durationTime;
		this.limitTime = limitTime;
		this.stepOrder = stepOrder;
	}
}
