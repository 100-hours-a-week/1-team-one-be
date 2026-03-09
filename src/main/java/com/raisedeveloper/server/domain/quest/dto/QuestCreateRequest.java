package com.raisedeveloper.server.domain.quest.dto;

import static com.raisedeveloper.server.global.exception.ErrorMessageConstants.*;

import java.time.LocalDateTime;

import com.raisedeveloper.server.domain.common.enums.QuestType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record QuestCreateRequest(
	@NotBlank(message = QUEST_NAME_REQUIRED_MESSAGE)
	@Size(max = 50, message = QUEST_NAME_TOO_LONG_MESSAGE)
	String name,
	@NotBlank(message = QUEST_IMAGE_PATH_REQUIRED_MESSAGE)
	String questImagePath,
	@NotNull(message = QUEST_TYPE_REQUIRED_MESSAGE)
	QuestType type,
	@Positive(message = QUEST_REWARD_EXP_MIN_MESSAGE)
	int rewardExp,
	@Positive(message = QUEST_TARGET_COUNT_MIN_MESSAGE)
	short targetCount,
	@NotNull(message = QUEST_FINISHED_AT_REQUIRED_MESSAGE)
	LocalDateTime finishedAt
) {
}
