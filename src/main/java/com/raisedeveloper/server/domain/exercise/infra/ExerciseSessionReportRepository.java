package com.raisedeveloper.server.domain.exercise.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSessionReport;

public interface ExerciseSessionReportRepository extends JpaRepository<ExerciseSessionReport, Long> {
}
