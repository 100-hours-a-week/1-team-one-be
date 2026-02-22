package com.raisedeveloper.server.domain.exercise.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.dto.CharacterDto;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionCompleteResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionValidListResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionValidResponse;
import com.raisedeveloper.server.domain.exercise.dto.QuestProgressDto;
import com.raisedeveloper.server.domain.routine.domain.RoutineStep;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExerciseSessionMapper {

	private final ExerciseMapper exerciseMapper;

	public ExerciseSessionResponse toSessionResponse(ExerciseSession session, List<RoutineStep> routineSteps) {
		return new ExerciseSessionResponse(
			session.getRoutine().getId(),
			session.getRoutine().getRoutineOrder(),
			session.getCreatedAt(),
			exerciseMapper.toRoutineStepResponses(routineSteps)
		);
	}

	public ExerciseSessionValidResponse toValidResponse(ExerciseSession session) {
		return new ExerciseSessionValidResponse(
			session.getId(),
			session.getRoutine().getId(),
			session.getCreatedAt()
		);
	}

	public ExerciseSessionValidListResponse toValidListResponse(List<ExerciseSession> sessions) {
		List<ExerciseSessionValidResponse> responses = sessions.stream()
			.map(this::toValidResponse)
			.toList();
		return new ExerciseSessionValidListResponse(responses.isEmpty() ? null : responses);
	}

	public CharacterDto toCharacterDto(UserCharacter character) {
		return new CharacterDto(
			character.getLevel(),
			character.getExp(),
			character.getStreak(),
			character.getStatusScore()
		);
	}

	public ExerciseSessionCompleteResponse toCompleteResponse(
		Long sessionId,
		int earnedExp,
		int earnedStatusScore,
		CharacterDto character,
		List<QuestProgressDto> quests
	) {
		return new ExerciseSessionCompleteResponse(
			sessionId,
			true,
			earnedExp,
			earnedStatusScore,
			character,
			quests
		);
	}
}
