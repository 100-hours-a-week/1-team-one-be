package com.raisedeveloper.server.domain.user.application;

import java.util.List;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;
import com.raisedeveloper.server.global.pagination.CursorTokenCodec;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LeaderboardCursorService {

	private final CursorTokenCodec cursorTokenCodec;

	public String encode(long snapshotVersion, long lastRank) {
		String payload = snapshotVersion + ":" + lastRank;
		return cursorTokenCodec.encode(payload);
	}

	public LeaderboardCursor decode(String cursor) {
		String payload = cursorTokenCodec.decode(cursor);
		if (payload == null) {
			return null;
		}

		String[] values = payload.split(":");
		if (values.length != 2) {
			throw invalidCursor();
		}

		try {
			long snapshotVersion = Long.parseLong(values[0]);
			long lastRank = Long.parseLong(values[1]);
			return new LeaderboardCursor(snapshotVersion, lastRank);
		} catch (NumberFormatException ex) {
			throw invalidCursor();
		}
	}

	private CustomException invalidCursor() {
		return new CustomException(
			ErrorCode.VALIDATION_FAILED,
			List.of(ErrorDetail.field("cursor", "invalid cursor"))
		);
	}
}
