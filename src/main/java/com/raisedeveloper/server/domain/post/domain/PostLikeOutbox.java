package com.raisedeveloper.server.domain.post.domain;

import java.time.LocalDateTime;

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
@Table(name = "post_like_outbox")
public class PostLikeOutbox extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long postId;

	@Column(nullable = false)
	private int delta;

	private LocalDateTime processedAt;

	public PostLikeOutbox(Long postId, int delta) {
		this.postId = postId;
		this.delta = delta;
	}
}
