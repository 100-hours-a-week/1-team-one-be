package com.raisedeveloper.server.domain.user.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.user.domain.LeaderboardSnapshotRank;
import com.raisedeveloper.server.domain.user.infra.LeaderboardRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardSnapshotService {

	private final LeaderboardRepository leaderboardRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public long rebuildSnapshot() {
		long snapshotVersion = System.currentTimeMillis();
		List<LeaderboardRepository.LiveLeaderboardProjection> rankings = leaderboardRepository.findCurrentLeaderboard();

		List<LeaderboardSnapshotRank> snapshotRanks = rankings.stream()
			.map(row -> new LeaderboardSnapshotRank(
				snapshotVersion,
				row.getRankNo(),
				row.getUserId(),
				row.getNickname(),
				row.getProfileImageUrl(),
				row.getLevel(),
				row.getExp(),
				row.getStatusScore(),
				row.getStreak(),
				row.getTotalExp()
			))
			.toList();

		leaderboardRepository.saveAll(snapshotRanks);
		log.info("Leaderboard snapshot rebuilt: version={}, size={}", snapshotVersion, snapshotRanks.size());
		return snapshotVersion;
	}

	@Transactional(readOnly = true)
	public Optional<Long> getLatestSnapshotVersion() {
		return leaderboardRepository.findLatestSnapshotVersion();
	}
}
