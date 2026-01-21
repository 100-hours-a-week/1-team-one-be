package com.raisedeveloper.server.domain.auth.domain;

import java.time.LocalDateTime;

import com.raisedeveloper.server.domain.common.domain.CreatedUpdatedEntity;
import com.raisedeveloper.server.domain.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "fcm_tokens")
public class FcmToken extends CreatedUpdatedEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String token;

	private LocalDateTime revokedAt;

	private LocalDateTime lastSentAt;

	public FcmToken(User user, String token) {
		this.user = user;
		this.token = token;
	}

	public void updateToken(String token) {
		this.token = token;
		this.revokedAt = null;
	}

	public void revoke() {
		this.revokedAt = LocalDateTime.now();
	}

	public void used() {
		this.lastSentAt = LocalDateTime.now();
	}

	public void used(LocalDateTime localDateTime) {
		this.lastSentAt = localDateTime;
	}

}
