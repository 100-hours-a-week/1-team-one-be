package com.raisedeveloper.server.domain.user.domain;

import com.raisedeveloper.server.domain.common.domain.CreatedUpdatedEntity;
import com.raisedeveloper.server.domain.user.enums.CharacterType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_characters")
public class UserCharacter extends CreatedUpdatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private CharacterType type;

	@Column(nullable = false, length = 10)
	private String name;

	@Column(nullable = false)
	private int level;

	@Column(nullable = false)
	private int exp;

	@Column(nullable = false)
	private int streak;

	@Column(nullable = false)
	private int statusScore;

	public UserCharacter(User user, CharacterType type) {
		this.user = user;
		this.type = type;
		this.name = type.name();
		this.level = 1;
		this.exp = 0;
		this.streak = 0;
		this.statusScore = 10;
	}
}
