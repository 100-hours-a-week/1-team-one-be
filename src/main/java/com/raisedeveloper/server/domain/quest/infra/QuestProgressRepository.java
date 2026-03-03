package com.raisedeveloper.server.domain.quest.infra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.quest.domain.QuestProgress;

@Repository
public interface QuestProgressRepository extends JpaRepository<QuestProgress, Long> {

	List<QuestProgress> findByUserIdAndCompletedAtIsNotNullOrderByCompletedAtDesc(Long userId);

	List<QuestProgress> findByUserIdAndCompletedAtIsNullAndQuestFinishedAtAfterOrderByQuestFinishedAtAsc(
		Long userId,
		LocalDateTime now
	);
}
