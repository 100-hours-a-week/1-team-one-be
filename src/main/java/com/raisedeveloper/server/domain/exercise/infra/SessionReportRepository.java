package com.raisedeveloper.server.domain.exercise.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raisedeveloper.server.domain.exercise.domain.SessionReport;

public interface SessionReportRepository extends JpaRepository<SessionReport, Long> {
}
