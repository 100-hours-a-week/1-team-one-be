package com.raisedeveloper.server.domain.survey.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.survey.domain.SurveySubmission;

@Repository
public interface SurveySubmissionRepository extends JpaRepository<SurveySubmission, Long> {
}
