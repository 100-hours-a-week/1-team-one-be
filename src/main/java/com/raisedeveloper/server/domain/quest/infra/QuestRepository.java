package com.raisedeveloper.server.domain.quest.infra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.quest.domain.Quest;

@Repository
public interface QuestRepository extends JpaRepository<Quest, Long> {

	List<Quest> findAllByFinishedAtAfterOrderByFinishedAtAsc(LocalDateTime dateTime);
}
