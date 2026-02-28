package com.raisedeveloper.server.domain.quest.domain;

import java.time.LocalDateTime;

import com.raisedeveloper.server.domain.common.domain.CreatedUpdatedEntity;
import com.raisedeveloper.server.domain.common.enums.QuestType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quests")
public class Quest extends CreatedUpdatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false)
	private String questImagePath;

	@Column(nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private QuestType type;

	@Column(nullable = false)
	private int rewardExp;

	@Column(nullable = false)
	private short targetCount;

	@Column(nullable = false)
	private LocalDateTime finishedAt;

	public Quest(
		String name,
		String questImagePath,
		QuestType type,
		int rewardExp,
		short targetCount,
		LocalDateTime finishedAt
	) {
		this.name = name;
		this.questImagePath = questImagePath;
		this.type = type;
		this.rewardExp = rewardExp;
		this.targetCount = targetCount;
		this.finishedAt = finishedAt;
	}
}
