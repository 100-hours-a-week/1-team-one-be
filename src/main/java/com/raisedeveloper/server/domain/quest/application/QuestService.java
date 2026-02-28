package com.raisedeveloper.server.domain.quest.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.quest.domain.Quest;
import com.raisedeveloper.server.domain.quest.domain.QuestProgress;
import com.raisedeveloper.server.domain.quest.dto.QuestCreateRequest;
import com.raisedeveloper.server.domain.quest.dto.QuestCreateResponse;
import com.raisedeveloper.server.domain.quest.dto.QuestItem;
import com.raisedeveloper.server.domain.quest.dto.QuestListResponse;
import com.raisedeveloper.server.domain.quest.infra.QuestProgressRepository;
import com.raisedeveloper.server.domain.quest.infra.QuestRepository;
import com.raisedeveloper.server.domain.quest.mapper.QuestMapper;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestService {

	private final QuestRepository questRepository;
	private final QuestProgressRepository questProgressRepository;
	private final UserRepository userRepository;
	private final QuestMapper questMapper;

	@Transactional
	public QuestCreateResponse createQuest(QuestCreateRequest request) {
		Quest quest = questRepository.save(
			new Quest(
				request.name(),
				request.questImagePath(),
				request.type(),
				request.rewardExp(),
				request.targetCount(),
				request.finishedAt()
			)
		);
		return questMapper.toCreateResponse(quest);
	}

	@Transactional
	public QuestListResponse getQuests(Long userId, Boolean isCompleted) {
		userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		List<QuestItem> quests;
		if (Boolean.TRUE.equals(isCompleted)) {
			quests = questProgressRepository.findByUserIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(userId)
				.stream()
				.map(questMapper::toQuestItem)
				.toList();
		} else {
			quests = getInProgressQuestProgresses(userId, LocalDateTime.now())
				.stream()
				.map(questMapper::toQuestItem)
				.toList();
		}
		return questMapper.toListResponse(quests);
	}

	private List<QuestProgress> getInProgressQuestProgresses(Long userId, LocalDateTime now) {
		return questProgressRepository.findByUserIdAndCompletedAtIsNullAndQuestFinishedAtAfterOrderByQuestFinishedAtAsc(
			userId,
			now
		);
	}
}
