package com.raisedeveloper.server.domain.user.application;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.user.domain.LeaderboardSnapshotRank;
import com.raisedeveloper.server.domain.user.dto.LeaderboardRankItem;
import com.raisedeveloper.server.domain.user.dto.LeaderboardResponse;
import com.raisedeveloper.server.domain.user.infra.LeaderboardRepository;
import com.raisedeveloper.server.domain.user.infra.UserCharacterRepository;
import com.raisedeveloper.server.domain.user.infra.UserProfileRepository;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;
import com.raisedeveloper.server.global.pagination.PaginationConstants;
import com.raisedeveloper.server.global.pagination.PagingResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {

	private static final int PODIUM_SIZE = 3;

	private final LeaderboardRepository leaderboardRepository;
	private final LeaderboardCursorService leaderboardCursorService;
	private final LeaderboardSnapshotService leaderboardSnapshotService;
	private final UserRepository userRepository;
	private final UserCharacterRepository userCharacterRepository;
	private final UserProfileRepository userProfileRepository;

	public LeaderboardResponse getLeaderboard(Long userId, Integer limit, String cursor) {
		int size = normalizeLimit(limit);
		LeaderboardCursor decoded = leaderboardCursorService.decode(cursor);
		long snapshotVersion = decoded == null
			? leaderboardSnapshotService.getLatestSnapshotVersion()
				.orElseGet(leaderboardSnapshotService::rebuildSnapshot)
			: decoded.snapshotVersion();

		if (!leaderboardRepository.existsBySnapshotVersion(snapshotVersion)) {
			throw new CustomException(
				ErrorCode.VALIDATION_FAILED,
				List.of(ErrorDetail.field("cursor", "snapshot is no longer available"))
			);
		}

		long lastRank = decoded == null ? 0L : decoded.lastRank();

		List<LeaderboardSnapshotRank> podiumRows =
			leaderboardRepository.findBySnapshotVersionAndRankNoLessThanEqualOrderByRankNoAsc(
				snapshotVersion,
				PODIUM_SIZE
			);

		List<LeaderboardSnapshotRank> rankRows =
			leaderboardRepository.findBySnapshotVersionAndRankNoGreaterThanOrderByRankNoAsc(
				snapshotVersion,
				lastRank,
				PageRequest.of(0, size + 1)
			);

		LeaderboardRankItem myRankItem = leaderboardRepository.findBySnapshotVersionAndUserId(
			snapshotVersion,
			userId
		)
			.map(this::toRankItem)
			.orElseGet(() -> resolveMyRankNotFoundOrNull(userId));

		boolean hasNext = rankRows.size() > size;
		List<LeaderboardSnapshotRank> slicedRows = rankRows.stream()
			.limit(size)
			.toList();
		String nextCursor = buildNextCursor(snapshotVersion, slicedRows, hasNext);

		return new LeaderboardResponse(
			podiumRows.stream().map(this::toRankItem).toList(),
			slicedRows.stream().map(this::toRankItem).toList(),
			myRankItem,
			new PagingResponse(nextCursor, hasNext)
		);
	}

	private int normalizeLimit(Integer limit) {
		if (limit == null) {
			return PaginationConstants.LEADERBOARD_DEFAULT_LIMIT;
		}
		if (limit < 1 || limit > PaginationConstants.LEADERBOARD_MAX_LIMIT) {
			throw new CustomException(
				ErrorCode.VALIDATION_FAILED,
				List.of(ErrorDetail.field("limit", "limit must be between 1 and 50"))
			);
		}
		return limit;
	}

	private String buildNextCursor(long snapshotVersion, List<LeaderboardSnapshotRank> rows, boolean hasNext) {
		if (!hasNext || rows.isEmpty()) {
			return null;
		}

		LeaderboardSnapshotRank last = rows.getLast();
		return leaderboardCursorService.encode(snapshotVersion, last.getRankNo());
	}

	private LeaderboardRankItem toRankItem(LeaderboardSnapshotRank projection) {
		return new LeaderboardRankItem(
			projection.getRankNo(),
			projection.getUserId(),
			projection.getNickname(),
			projection.getProfileImageUrl(),
			projection.getLevel(),
			projection.getExp(),
			projection.getStatusScore(),
			projection.getStreak()
		);
	}

	private LeaderboardRankItem resolveMyRankNotFoundOrNull(Long userId) {
		userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		userProfileRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
		userCharacterRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHARACTER_NOT_SET));
		return null;
	}
}
