package com.raisedeveloper.server.domain.exercise.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.exercise.domain.ExerciseSessionReport;
import com.raisedeveloper.server.domain.exercise.dto.CharacterDto;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionCompleteResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionReportCreateResponse;
import com.raisedeveloper.server.domain.exercise.dto.ExerciseSessionUpdateRequest;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionReportRepository;
import com.raisedeveloper.server.domain.exercise.mapper.ExerciseSessionMapper;
import com.raisedeveloper.server.domain.notification.application.NotificationService;
import com.raisedeveloper.server.domain.user.application.SessionRewardResult;
import com.raisedeveloper.server.domain.user.application.UserCharacterService;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.infra.UserCharacterRepository;
import com.raisedeveloper.server.global.exception.CustomException;
import com.raisedeveloper.server.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseSessionFacade {

	private final ExerciseSessionService exerciseSessionService;
	private final UserCharacterService userCharacterService;
	private final NotificationService notificationService;
	private final ExerciseSessionReportRepository exerciseSessionReportRepository;
	private final UserCharacterRepository userCharacterRepository;
	private final ExerciseSessionMapper exerciseSessionMapper;

	@Transactional
	public ExerciseSessionCompleteResponse completeExerciseSession(
		Long userId,
		Long sessionId,
		ExerciseSessionUpdateRequest request
	) {
		SessionCompletionContext completion = completeSession(userId, sessionId, request);

		int earnedExp = completion.rewardResult().earnedExp();
		int earnedStatusScore = completion.rewardResult().earnedStatusScore();
		CharacterDto characterDto = exerciseSessionMapper.toCharacterDto(completion.rewardResult().character());

		log.info("Exercise session completed: sessionId={}, userId={}, earnedExp={}, earnedStatusScore={}",
			sessionId, userId, earnedExp, earnedStatusScore);

		if (completion.completedCount() == 0) {
			notificationService.createStretchingFailed(completion.session().getUser());
		} else {
			notificationService.createStretchingSuccess(completion.session().getUser(), earnedExp);
		}

		return exerciseSessionMapper.toCompleteResponse(
			sessionId,
			earnedExp,
			earnedStatusScore,
			characterDto,
			List.of() // TODO: Quest 기능 구현 시 실제 퀘스트 진행도 반환
		);
	}

	@Transactional
	public ExerciseSessionReportCreateResponse completeExerciseSessionV2(
		Long userId,
		Long sessionId,
		ExerciseSessionUpdateRequest request
	) {
		SessionCompletionContext completion = completeSession(userId, sessionId, request);

		UserCharacter character = completion.rewardResult().character();
		ExerciseSessionReport exerciseSessionReport = exerciseSessionReportRepository.save(
			new ExerciseSessionReport(
				completion.session(),
				completion.session().getUser(),
				character.getLevel(),
				completion.previousExp(),
				completion.rewardResult().earnedExp(),
				character.getStreak(),
				completion.previousStatusScore(),
				completion.rewardResult().earnedStatusScore()
			)
		);

		log.info("Exercise session v2 completed and report created: sessionId={}, userId={}, reportId={}",
			sessionId, userId, exerciseSessionReport.getId());

		if (completion.completedCount() == 0) {
			notificationService.createStretchingFailed(completion.session().getUser());
		} else {
			notificationService.createStretchingSuccess(
				completion.session().getUser(),
				completion.rewardResult().earnedExp()
			);
		}

		return new ExerciseSessionReportCreateResponse(
			completion.session().getId(),
			exerciseSessionReport.getId(),
			completion.session().getRoutine().getId(),
			completion.session().getStartAt(),
			completion.session().getEndAt(),
			completion.session().getIsRoutineCompleted()
		);
	}

	private SessionCompletionContext completeSession(
		Long userId,
		Long sessionId,
		ExerciseSessionUpdateRequest request
	) {
		ExerciseSession session = exerciseSessionService.getSessionForUpdate(userId, sessionId);
		long completedCount = exerciseSessionService.updateSessionAndResults(session, request);
		boolean hasCompletedToday = exerciseSessionService.hasCompletedSessionToday(userId);

		UserCharacter beforeReward = userCharacterRepository.findByUserId(userId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHARACTER_NOT_SET));
		int previousExp = beforeReward.getExp();
		int previousStatusScore = beforeReward.getStatusScore();

		SessionRewardResult rewardResult = userCharacterService.applySessionReward(
			userId,
			completedCount,
			hasCompletedToday
		);

		return new SessionCompletionContext(
			session,
			completedCount,
			previousExp,
			previousStatusScore,
			rewardResult
		);
	}

	private record SessionCompletionContext(
		ExerciseSession session,
		long completedCount,
		int previousExp,
		int previousStatusScore,
		SessionRewardResult rewardResult
	) {
	}
}
