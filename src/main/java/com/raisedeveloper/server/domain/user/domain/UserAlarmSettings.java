package com.raisedeveloper.server.domain.user.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.raisedeveloper.server.domain.common.domain.CreatedUpdatedEntity;

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
@Table(name = "user_alarm_settings")
public class UserAlarmSettings extends CreatedUpdatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private int alarmInterval;

	@Column(nullable = false)
	private LocalTime activeStartAt;

	@Column(nullable = false)
	private LocalTime activeEndAt;

	@Column(nullable = false)
	private LocalTime focusStartAt;

	@Column(nullable = false)
	private LocalTime focusEndAt;

	@Column(nullable = false)
	private String repeatDays;

	@Column(nullable = false)
	private boolean dnd;

	private LocalDateTime dndFinishedAt;

	public UserAlarmSettings(
		User user,
		short interval,
		LocalTime activeStartAt,
		LocalTime activeEndAt,
		LocalTime focusStartAt,
		LocalTime focusEndAt,
		String repeatDays,
		LocalDateTime dndFinishedAt
	) {
		this.user = user;
		this.alarmInterval = interval;
		this.activeStartAt = activeStartAt;
		this.activeEndAt = activeEndAt;
		this.focusStartAt = focusStartAt;
		this.focusEndAt = focusEndAt;
		this.repeatDays = repeatDays;
		this.dndFinishedAt = dndFinishedAt;
		this.dnd = false;
	}

	public void updateSettings(
		int interval,
		LocalTime activeStartAt,
		LocalTime activeEndAt,
		LocalTime focusStartAt,
		LocalTime focusEndAt,
		String repeatDays,
		LocalDateTime dndFinishedAt
	) {
		this.alarmInterval = interval;
		this.activeStartAt = activeStartAt;
		this.activeEndAt = activeEndAt;
		this.focusStartAt = focusStartAt;
		this.focusEndAt = focusEndAt;
		this.repeatDays = repeatDays;
		this.dndFinishedAt = dndFinishedAt;
	}

	public void enableDnd(LocalDateTime dndFinishedAt) {
		this.dnd = true;
		this.dndFinishedAt = dndFinishedAt;
	}
}
