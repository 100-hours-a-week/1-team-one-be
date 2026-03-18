package com.raisedeveloper.server.domain.quest.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.dto.QuestProgressDto;
import com.raisedeveloper.server.domain.quest.domain.Quest;
import com.raisedeveloper.server.domain.quest.domain.QuestProgress;
import com.raisedeveloper.server.domain.quest.infra.QuestProgressRepository;
import com.raisedeveloper.server.domain.quest.infra.QuestRepository;
import com.raisedeveloper.server.domain.quest.mapper.QuestMapper;
import com.raisedeveloper.server.domain.user.application.UserCharacterService;
import com.raisedeveloper.server.domain.user.domain.User;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.infra.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestProgressService {

	private final QuestRepository questRepository;
	private final QuestProgressRepository questProgressRepository;
	private final UserRepository userRepository;
	private final UserCharacterService userCharacterService;
	private final QuestMapper questMapper;

	@Transactional
	public List<QuestProgressDto> updateStretchingStreakQuests(Long userId, LocalDateTime occurredAt) {
		LocalDate activityDate = occurredAt.toLocalDate();
		List<Quest> quests = questRepository.findAllByFinishedAtAfterOrderByFinishedAtAsc(occurredAt);
		if (quests.isEmpty()) {
			return List.of();
		}

		User user = userRepository.getReferenceById(userId);
		UserCharacter character = userCharacterService.getByUserIdOrThrow(userId);

		return quests.stream()
			.map(quest -> getOrCreateProgress(user, quest))
			.filter(questProgress -> applyStretchingStreakProgress(questProgress, character, activityDate, occurredAt))
			.map(questMapper::toProgressDto)
			.toList();
	}

	private boolean applyStretchingStreakProgress(
		QuestProgress questProgress,
		UserCharacter character,
		LocalDate activityDate,
		LocalDateTime occurredAt
	) {
		if (questProgress.isCompleted() || activityDate.equals(questProgress.getLastProgressedOn())) {
			return false;
		}

		LocalDate lastProgressedOn = questProgress.getLastProgressedOn();
		if (lastProgressedOn != null && lastProgressedOn.plusDays(1).equals(activityDate)) {
			questProgress.increaseCount();
		} else {
			questProgress.resetCount((short)1);
		}

		questProgress.updateLastProgressedOn(activityDate);
		boolean completedNow = questProgress.completeIfTargetReached(occurredAt);
		if (completedNow) {
			character.addExp(questProgress.getQuest().getRewardExp());
		}
		return true;
	}

	private QuestProgress getOrCreateProgress(User user, Quest quest) {
		return questProgressRepository.findByUserIdAndQuestId(user.getId(), quest.getId())
			.orElseGet(() -> questProgressRepository.save(new QuestProgress(user, quest)));
	}
}
