package com.raisedeveloper.server.domain.survey.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.survey.domain.SurveyQuestion;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {
}
