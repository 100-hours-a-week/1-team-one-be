package com.raisedeveloper.server.domain.survey.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.survey.domain.Survey;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
	Optional<Survey> findFirstByIsActiveTrueOrderByVersionDesc();
}
