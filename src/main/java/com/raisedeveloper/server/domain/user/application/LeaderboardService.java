package com.raisedeveloper.server.domain.user.application;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.user.domain.LeaderboardSnapshotRank;
import com.raisedeveloper.server.domain.user.dto.LeaderboardRankItem;
import com.raisedeveloper.server.domain.user.dto.LeaderboardResponse;
import com.raisedeveloper.server.domain.user.enums.LeaderboardDirection;
import com.raisedeveloper.server.domain.user.infra.LeaderboardRepository;
import com.raisedeveloper.server.domain.user.infra.UserCharacterRepository;
import com.raisedeveloper.server.domain.user.infra.UserProfileRepository;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;
import com.raisedeveloper.server.global.pagination.BiDirectionPagingResponse;
import com.raisedeveloper.server.global.pagination.PaginationConstants;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {

	private static final int PODIUM_SIZE = 3;
	private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

	private final LeaderboardRepository leaderboardRepository;
	private final LeaderboardCursorService leaderboardCursorService;
	private final LeaderboardSnapshotService leaderboardSnapshotService;
	private final UserRepository userRepository;
	private final UserCharacterRepository userCharacterRepository;
	private final UserProfileRepository userProfileRepository;

	public LeaderboardResponse getLeaderboard(Long userId, Integer limit, String cursor,
		LeaderboardDirection direction) {
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

		long maxRank = leaderboardRepository.findMaxRankNoBySnapshotVersion(snapshotVersion)
			.orElse(0L);

		List<LeaderboardSnapshotRank> podiumRows =
			leaderboardRepository.findBySnapshotVersionAndRankNoLessThanEqualOrderByRankNoAsc(
				snapshotVersion,
				PODIUM_SIZE
			);

		LeaderboardRankItem myRankItem = leaderboardRepository.findBySnapshotVersionAndUserId(
			snapshotVersion,
			userId
		)
			.map(this::toRankItem)
			.orElseGet(() -> resolveMyRankNotFoundOrNull(userId));

		WindowResult window = decoded == null
			? loadInitialWindow(snapshotVersion, size, myRankItem, maxRank)
			: loadDirectionalWindow(snapshotVersion, size, decoded.lastRank(), direction, maxRank);

		return new LeaderboardResponse(
			podiumRows.stream().map(this::toRankItem).toList(),
			window.rows().stream().map(this::toRankItem).toList(),
			myRankItem,
			new BiDirectionPagingResponse(window.prevCursor(), window.nextCursor(), window.hasPrev(), window.hasNext()),
			toLastUpdatedAt(snapshotVersion)
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

	private WindowResult loadInitialWindow(
		long snapshotVersion,
		int size,
		LeaderboardRankItem myRankItem,
		long maxRank
	) {
		if (maxRank == 0) {
			return new WindowResult(List.of(), null, null, false, false);
		}

		if (myRankItem == null) {
			List<LeaderboardSnapshotRank> rows = leaderboardRepository
				.findBySnapshotVersionAndRankNoGreaterThanOrderByRankNoAsc(
					snapshotVersion,
					0L,
					PageRequest.of(0, size)
				);
			return buildWindowResult(snapshotVersion, rows, maxRank);
		}

		long ranksAbove = size / 2L;
		long ranksBelow = size - ranksAbove - 1L;
		long startRank = Math.max(1L, myRankItem.rank() - ranksAbove);
		long endRank = Math.min(maxRank, myRankItem.rank() + ranksBelow);

		long actualWindowSize = endRank - startRank + 1L;
		if (actualWindowSize < size) {
			long missing = size - actualWindowSize;
			startRank = Math.max(1L, startRank - missing);
			endRank = Math.min(maxRank, startRank + size - 1L);
			startRank = Math.max(1L, endRank - size + 1L);
		}

		List<LeaderboardSnapshotRank> rows = leaderboardRepository
			.findBySnapshotVersionAndRankNoBetweenOrderByRankNoAsc(snapshotVersion, startRank, endRank);
		return buildWindowResult(snapshotVersion, rows, maxRank);
	}

	private WindowResult loadDirectionalWindow(
		long snapshotVersion,
		int size,
		long boundaryRank,
		LeaderboardDirection direction,
		long maxRank
	) {
		LeaderboardDirection resolvedDirection = direction == null ? LeaderboardDirection.NEXT : direction;
		if (resolvedDirection == LeaderboardDirection.PREV) {
			List<LeaderboardSnapshotRank> rows = leaderboardRepository
				.findBySnapshotVersionAndRankNoLessThanOrderByRankNoDesc(
					snapshotVersion,
					boundaryRank,
					PageRequest.of(0, size + 1)
				);
			boolean hasPrev = rows.size() > size;
			List<LeaderboardSnapshotRank> sliced = new ArrayList<>(rows.stream().limit(size).toList());
			Collections.reverse(sliced);
			return buildWindowResult(snapshotVersion, sliced, maxRank, hasPrev);
		}

		List<LeaderboardSnapshotRank> rows = leaderboardRepository
			.findBySnapshotVersionAndRankNoGreaterThanOrderByRankNoAsc(
				snapshotVersion,
				boundaryRank,
				PageRequest.of(0, size + 1)
			);
		boolean hasNext = rows.size() > size;
		List<LeaderboardSnapshotRank> sliced = rows.stream().limit(size).toList();
		return buildWindowResult(snapshotVersion, sliced, maxRank, null, hasNext);
	}

	private WindowResult buildWindowResult(
		long snapshotVersion,
		List<LeaderboardSnapshotRank> rows,
		long maxRank
	) {
		return buildWindowResult(snapshotVersion, rows, maxRank, null, null);
	}

	private WindowResult buildWindowResult(
		long snapshotVersion,
		List<LeaderboardSnapshotRank> rows,
		long maxRank,
		Boolean forcedHasPrev
	) {
		return buildWindowResult(snapshotVersion, rows, maxRank, forcedHasPrev, null);
	}

	private WindowResult buildWindowResult(
		long snapshotVersion,
		List<LeaderboardSnapshotRank> rows,
		long maxRank,
		Boolean forcedHasPrev,
		Boolean forcedHasNext
	) {
		if (rows.isEmpty()) {
			return new WindowResult(List.of(), null, null, false, false);
		}

		long firstRank = rows.getFirst().getRankNo();
		long lastRank = rows.getLast().getRankNo();
		boolean hasPrev = forcedHasPrev != null ? forcedHasPrev : firstRank > 1;
		boolean hasNext = forcedHasNext != null ? forcedHasNext : lastRank < maxRank;
		String prevCursor = hasPrev ? leaderboardCursorService.encode(snapshotVersion, firstRank) : null;
		String nextCursor = hasNext ? leaderboardCursorService.encode(snapshotVersion, lastRank) : null;
		return new WindowResult(rows, prevCursor, nextCursor, hasPrev, hasNext);
	}

	private record WindowResult(
		List<LeaderboardSnapshotRank> rows,
		String prevCursor,
		String nextCursor,
		boolean hasPrev,
		boolean hasNext
	) {
	}

	private LocalDateTime toLastUpdatedAt(long snapshotVersion) {
		return Instant.ofEpochMilli(snapshotVersion)
			.atZone(ZONE_ID)
			.toLocalDateTime();
	}
}
