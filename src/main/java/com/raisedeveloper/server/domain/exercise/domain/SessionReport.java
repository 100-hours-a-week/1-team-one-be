package com.raisedeveloper.server.domain.exercise.domain;

import com.raisedeveloper.server.domain.common.domain.CreatedUpdatedEntity;
import com.raisedeveloper.server.domain.user.domain.User;

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
@Table(name = "session_reports")
public class SessionReport extends CreatedUpdatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercise_session_id", nullable = false)
	private ExerciseSession exerciseSession;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private short level;

	private int previousExp;

	private int earnedExp;

	private int streak;

	private int previousStatusScore;

	private int earnedStatusScore;

	public SessionReport(
		ExerciseSession exerciseSession,
		User user,
		short level,
		int previousExp,
		int earnedExp,
		int streak,
		int previousStatusScore,
		int earnedStatusScore
	) {
		this.exerciseSession = exerciseSession;
		this.user = user;
		this.level = level;
		this.previousExp = previousExp;
		this.earnedExp = earnedExp;
		this.streak = streak;
		this.previousStatusScore = previousStatusScore;
		this.earnedStatusScore = earnedStatusScore;
	}
}
