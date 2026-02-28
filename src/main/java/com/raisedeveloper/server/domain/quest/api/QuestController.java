package com.raisedeveloper.server.domain.quest.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.raisedeveloper.server.domain.quest.application.QuestService;
import com.raisedeveloper.server.domain.quest.dto.QuestCreateRequest;
import com.raisedeveloper.server.domain.quest.dto.QuestCreateResponse;
import com.raisedeveloper.server.domain.quest.dto.QuestListResponse;
import com.raisedeveloper.server.global.response.ApiResponse;
import com.raisedeveloper.server.global.security.currentuser.CurrentUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class QuestController {

	private final QuestService questService;

	@PostMapping("/quests")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<QuestCreateResponse> createQuest(@Valid @RequestBody QuestCreateRequest request) {
		QuestCreateResponse response = questService.createQuest(request);
		return ApiResponse.of("CREATE_QUEST_SUCCESS", response);
	}

	@GetMapping("/me/quests")
	public ApiResponse<QuestListResponse> getQuests(
		@CurrentUser Long userId,
		@RequestParam(value = "is-completed", required = false) Boolean isCompleted
	) {
		QuestListResponse response = questService.getQuests(userId, isCompleted);
		return ApiResponse.of("GET_ME_QUESTS_SUCCESS", response);
	}
}
