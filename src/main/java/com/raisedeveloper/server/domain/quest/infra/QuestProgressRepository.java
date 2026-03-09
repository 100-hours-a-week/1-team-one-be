package com.raisedeveloper.server.domain.quest.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.quest.domain.QuestProgress;

@Repository
public interface QuestProgressRepository extends JpaRepository<QuestProgress, Long> {

	List<QuestProgress> findByUserIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(Long userId);

	List<QuestProgress> findAllByUserIdAndQuestIdIn(Long userId, List<Long> questIds);

	Optional<QuestProgress> findByUserIdAndQuestId(Long userId, Long questId);
}
