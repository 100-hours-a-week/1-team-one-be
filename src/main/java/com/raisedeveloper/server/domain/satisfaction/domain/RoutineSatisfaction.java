package com.raisedeveloper.server.domain.satisfaction.domain;

import com.raisedeveloper.server.domain.common.domain.CreatedUpdatedEntity;
import com.raisedeveloper.server.domain.routine.domain.Routine;
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
@Table(name = "routine_satisfactions")
public class RoutineSatisfaction extends CreatedUpdatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "routine_id", nullable = false)
	private Routine routine;

	private byte satisfaction;

	public RoutineSatisfaction(User user, Routine routine, byte satisfaction) {
		this.user = user;
		this.routine = routine;
		this.satisfaction = satisfaction;
	}

	public void updateSatisfaction(byte satisfaction) {
		this.satisfaction = satisfaction;
	}
}
