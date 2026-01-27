package com.raisedeveloper.server.domain.survey.domain;

import com.raisedeveloper.server.domain.common.domain.CreatedAtEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "surveys")
public class Survey extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private short version;

	@Column(nullable = false)
	private boolean isActive;

	public Survey(short version) {
		this.version = version;
		this.isActive = true;
	}

	public void surveyInActivated() {
		this.isActive = false;
	}
}
