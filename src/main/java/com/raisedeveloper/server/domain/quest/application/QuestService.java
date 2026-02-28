package com.raisedeveloper.server.domain.quest.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.quest.domain.Quest;
import com.raisedeveloper.server.domain.quest.dto.QuestCreateRequest;
import com.raisedeveloper.server.domain.quest.dto.QuestCreateResponse;
import com.raisedeveloper.server.domain.quest.infra.QuestRepository;
import com.raisedeveloper.server.domain.quest.mapper.QuestMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestService {

	private final QuestRepository questRepository;
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
}
