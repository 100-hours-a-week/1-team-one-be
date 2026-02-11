package com.raisedeveloper.server.domain.post.domain;

import com.raisedeveloper.server.domain.common.domain.SoftDeleteEntity;
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
@Table(name = "posts")
public class Post extends SoftDeleteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 50)
	private String title;

	@Column(nullable = false, length = 500)
	private String content;

	@Column(nullable = false)
	private int likeCount;

	private String thumbnailImagePath;

	public Post(User user, String title, String content, String thumbnailImagePath) {
		this.user = user;
		this.title = title;
		this.content = content;
		this.thumbnailImagePath = thumbnailImagePath;
		this.likeCount = 0;
	}

	public void update(String title, String content, String thumbnailImagePath) {
		this.title = title;
		this.content = content;
		this.thumbnailImagePath = thumbnailImagePath;
	}
}
