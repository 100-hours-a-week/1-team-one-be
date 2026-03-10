package com.raisedeveloper.server.domain.user.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.user.domain.LeaderboardSnapshotRank;

@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardSnapshotRank, Long> {

	@Query(value = """
		WITH leaderboard AS (
			SELECT
				ROW_NUMBER() OVER (
					ORDER BY (uc.level * 1000 + uc.exp) DESC, uc.status_score DESC, u.id ASC
				) AS rank_no,
				u.id AS userId,
				up.nickname AS nickname,
				up.image_path AS profileImageUrl,
				uc.level AS level,
				uc.exp AS exp,
				uc.status_score AS statusScore,
				uc.streak AS streak,
				(uc.level * 1000 + uc.exp) AS totalExp
			FROM users u
			JOIN user_profiles up ON up.user_id = u.id
			JOIN user_characters uc ON uc.user_id = u.id
			WHERE u.deleted_at IS NULL
		)
		SELECT
			rank_no,
			userId,
			nickname,
			profileImageUrl,
			level,
			exp,
			statusScore,
			streak,
			totalExp
		FROM leaderboard
		ORDER BY rank_no ASC
		""", nativeQuery = true)
	List<LiveLeaderboardProjection> findCurrentLeaderboard();

	@Query("select max(lsr.snapshotVersion) from LeaderboardSnapshotRank lsr")
	Optional<Long> findLatestSnapshotVersion();

	List<LeaderboardSnapshotRank> findBySnapshotVersionAndRankNoLessThanEqualOrderByRankNoAsc(
		long snapshotVersion,
		long rankNo
	);

	List<LeaderboardSnapshotRank> findBySnapshotVersionAndRankNoGreaterThanOrderByRankNoAsc(
		long snapshotVersion,
		long rankNo,
		Pageable pageable
	);

	Optional<LeaderboardSnapshotRank> findBySnapshotVersionAndUserId(long snapshotVersion, Long userId);

	@Query("""
		select count(lsr) > 0
		from LeaderboardSnapshotRank lsr
		where lsr.snapshotVersion = :snapshotVersion
		""")
	boolean existsBySnapshotVersion(@Param("snapshotVersion") long snapshotVersion);

	interface LiveLeaderboardProjection {
		long getRankNo();

		Long getUserId();

		String getNickname();

		String getProfileImageUrl();

		short getLevel();

		int getExp();

		int getStatusScore();

		int getStreak();

		long getTotalExp();
	}
}
