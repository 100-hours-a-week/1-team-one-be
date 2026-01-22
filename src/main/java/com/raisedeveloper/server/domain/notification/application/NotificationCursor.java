package com.raisedeveloper.server.domain.notification.application;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;
import com.raisedeveloper.server.global.exception.ErrorDetail;
import com.raisedeveloper.server.global.pagination.CursorTokenCodec;

@Component
public class NotificationCursor {

	private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

	private final CursorTokenCodec cursorTokenCodec;

	public NotificationCursor(CursorTokenCodec cursorTokenCodec) {
		this.cursorTokenCodec = cursorTokenCodec;
	}

	public String encode(LocalDateTime createdAt, Long id) {
		long epochSecond = createdAt.atZone(ZONE_ID).toEpochSecond();
		String payload = epochSecond + ":" + id;
		return cursorTokenCodec.encode(payload);
	}

	public Cursor decode(String cursor) {
		String payload = cursorTokenCodec.decode(cursor);
		if (payload == null) {
			return null;
		}
		String[] values = payload.split(":");
		if (values.length != 2) {
			throw invalidCursor();
		}
		try {
			long epochSecond = Long.parseLong(values[0]);
			long id = Long.parseLong(values[1]);
			LocalDateTime createdAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ZONE_ID);
			return new Cursor(createdAt, id);
		} catch (NumberFormatException ex) {
			throw invalidCursor();
		}
	}

	private CustomException invalidCursor() {
		return new CustomException(
			ErrorCode.VALIDATION_FAILED,
			java.util.List.of(ErrorDetail.field("cursor", "invalid cursor"))
		);
	}

	public record Cursor(
		LocalDateTime createdAt,
		Long id
	) {
	}
}
