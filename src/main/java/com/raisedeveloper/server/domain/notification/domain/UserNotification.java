package com.raisedeveloper.server.domain.notification.domain;

import com.raisedeveloper.server.domain.common.domain.CreatedAtEntity;
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
@Table(name = "user_notifications")
public class UserNotification extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private String content;

	private String details;

	@Column(name = "is_read", nullable = false)
	private boolean isRead;

	public UserNotification(User user, String content, String details) {
		this.user = user;
		this.content = content;
		this.details = details;
		this.isRead = false;
	}

	public void markRead() {
		this.isRead = true;
	}
}
