package com.raisedeveloper.server.domain.exercise.dto;

import java.util.List;

public record ExerciseSessionCompleteResponse(
	Long sessionId,
	boolean isCompleted,
	int earnedExp,
	int earnedStatusScore,
	CharacterDto character,
	List<QuestProgressDto> quests
) {
}
