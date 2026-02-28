package com.raisedeveloper.server.domain.quest.dto;

import java.time.LocalDateTime;

import com.raisedeveloper.server.domain.common.enums.QuestType;

public record QuestItem(
	Long questId,
	String name,
	String questImagePath,
	QuestType type,
	int rewardExp,
	int targetCount,
	int currentCount,
	LocalDateTime finishedAt
) {
}
