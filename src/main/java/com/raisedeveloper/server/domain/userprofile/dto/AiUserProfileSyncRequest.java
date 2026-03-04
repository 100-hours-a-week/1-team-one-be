package com.raisedeveloper.server.domain.userprofile.dto;

import java.util.List;

public record AiUserProfileSyncRequest(
	List<AiUserProfileDto> profiles
) {
}
