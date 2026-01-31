package com.raisedeveloper.server.domain.exercise.domain;

import com.raisedeveloper.server.domain.common.domain.CreatedUpdatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "exercises")
public class Exercise extends CreatedUpdatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, length = 500)
	private String content;

	@Column(nullable = false, length = 500)
	private String effect;

	@Column(nullable = false)
	@Enumerated(value = EnumType.STRING)
	private ExerciseType type;

	@Column(nullable = false, columnDefinition = "json")
	private String pose;

	@Column(nullable = false, length = 50)
	private String bodyPart;

	@Column(nullable = false)
	private byte difficulty;

	@Column(nullable = false, length = 500)
	private String tags;

	@Column(nullable = false)
	private boolean isDeprecated;

	public Exercise(
		String name,
		String content,
		String effect,
		ExerciseType type,
		String pose,
		String bodyPart,
		byte difficulty,
		String tags
	) {
		this.name = name;
		this.content = content;
		this.effect = effect;
		this.type = type;
		this.pose = pose;
		this.bodyPart = bodyPart;
		this.difficulty = difficulty;
		this.tags = tags;
	}
}
