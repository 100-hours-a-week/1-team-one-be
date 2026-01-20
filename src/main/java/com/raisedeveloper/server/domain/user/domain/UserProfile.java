package com.raisedeveloper.server.domain.user.domain;

import com.raisedeveloper.server.domain.common.domain.CreatedUpdatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile extends CreatedUpdatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@Column(nullable = false)
	private String imagePath;

	@Column(nullable = false, length = 10, unique = true)
	private String nickname;

	public UserProfile(User user, String nickname, String imagePath) {
		this.user = user;
		this.nickname = nickname;
		this.imagePath = imagePath;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updateImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
}
