package com.raisedeveloper.server.domain.quest.dto;

import java.util.List;

public record QuestListResponse(
	List<QuestItem> quests
) {
}
