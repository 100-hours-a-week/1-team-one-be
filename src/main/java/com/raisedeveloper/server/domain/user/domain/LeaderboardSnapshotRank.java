package com.raisedeveloper.server.domain.user.domain;

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
@Table(name = "leaderboard_snapshot_ranks")
public class LeaderboardSnapshotRank extends CreatedAtEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private long snapshotVersion;

	@Column(nullable = false)
	private long rankNo;

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false, length = 10)
	private String nickname;

	@Column(nullable = false)
	private String profileImageUrl;

	@Column(nullable = false)
	private short level;

	@Column(nullable = false)
	private int exp;

	@Column(nullable = false)
	private int statusScore;

	@Column(nullable = false)
	private int streak;

	@Column(nullable = false)
	private long totalExp;

	public LeaderboardSnapshotRank(
		long snapshotVersion,
		long rankNo,
		Long userId,
		String nickname,
		String profileImageUrl,
		short level,
		int exp,
		int statusScore,
		int streak,
		long totalExp
	) {
		this.snapshotVersion = snapshotVersion;
		this.rankNo = rankNo;
		this.userId = userId;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
		this.level = level;
		this.exp = exp;
		this.statusScore = statusScore;
		this.streak = streak;
		this.totalExp = totalExp;
	}
}
