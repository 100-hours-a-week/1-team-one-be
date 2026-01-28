package com.raisedeveloper.server.domain.exercise.domain;

import java.time.LocalDateTime;

import com.raisedeveloper.server.domain.common.domain.CreatedUpdatedEntity;
import com.raisedeveloper.server.domain.routine.domain.Routine;
import com.raisedeveloper.server.domain.user.domain.User;

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
@Table(name = "exercise_sessions")
public class ExerciseSession extends CreatedUpdatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Routine routine;

	private LocalDateTime startAt;

	private LocalDateTime endAt;

	@Column(nullable = false)
	private boolean isRoutineCompleted;

	public ExerciseSession(User user, Routine routine) {
		this.user = user;
		this.routine = routine;
		this.isRoutineCompleted = false;
	}

	public void sessionCompleted(LocalDateTime startAt, LocalDateTime endAt) {
		this.startAt = startAt;
		this.endAt = endAt;
		this.isRoutineCompleted = true;
	}

	public void sessionFailed(LocalDateTime failedAt) {
		this.endAt = failedAt;
		this.isRoutineCompleted = false;
	}

	public void updateSession(LocalDateTime startAt, LocalDateTime endAt, boolean isRoutineCompleted) {
		this.startAt = startAt;
		this.endAt = endAt;
		this.isRoutineCompleted = isRoutineCompleted;
	}
}
