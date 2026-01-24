package com.raisedeveloper.server.domain.survey.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.survey.domain.SurveyOption;

@Repository
public interface SurveyOptionRepository extends JpaRepository<SurveyOption, Long> {
	List<SurveyOption> findAllBySurveyQuestionIdIn(
		List<Long> surveyQuestionIds
	);
}
