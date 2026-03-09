package com.raisedeveloper.server.domain.quest.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.exercise.dto.QuestProgressDto;
import com.raisedeveloper.server.domain.quest.domain.Quest;
import com.raisedeveloper.server.domain.quest.domain.QuestProgress;
import com.raisedeveloper.server.domain.quest.dto.QuestCreateResponse;
import com.raisedeveloper.server.domain.quest.dto.QuestItem;
import com.raisedeveloper.server.domain.quest.dto.QuestListResponse;

@Component
public class QuestMapper {

	public QuestCreateResponse toCreateResponse(Quest quest) {
		return new QuestCreateResponse(quest.getId());
	}

	public QuestItem toQuestItem(QuestProgress progress) {
		Quest quest = progress.getQuest();
		return toQuestItem(quest, progress);
	}

	public QuestItem toQuestItem(Quest quest, QuestProgress progress) {
		return new QuestItem(
			quest.getId(),
			quest.getName(),
			quest.getQuestImagePath(),
			quest.getType(),
			quest.getRewardExp(),
			quest.getTargetCount(),
			progress == null ? 0 : progress.getCurrentCount(),
			quest.getFinishedAt()
		);
	}

	public QuestProgressDto toProgressDto(QuestProgress progress) {
		Quest quest = progress.getQuest();
		return new QuestProgressDto(
			quest.getId(),
			quest.getName(),
			quest.getTargetCount(),
			progress.getCurrentCount()
		);
	}

	public QuestListResponse toListResponse(List<QuestItem> quests) {
		return new QuestListResponse(quests);
	}
}
