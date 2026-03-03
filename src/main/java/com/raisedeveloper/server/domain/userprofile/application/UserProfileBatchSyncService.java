package com.raisedeveloper.server.domain.userprofile.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseType;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseRepository;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseResultRepository;
import com.raisedeveloper.server.domain.exercise.infra.ExerciseSessionRepository;
import com.raisedeveloper.server.domain.user.infra.UserRepository;
import com.raisedeveloper.server.domain.userprofile.client.AiUserProfileClient;
import com.raisedeveloper.server.domain.userprofile.dto.AiUserProfileDto;
import com.raisedeveloper.server.domain.userprofile.dto.AiUserProfileSyncRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileBatchSyncService {

	private static final List<String> DEFAULT_DIFFICULTIES = List.of("1", "2", "3");

	private final UserRepository userRepository;
	private final ExerciseRepository exerciseRepository;
	private final ExerciseResultRepository exerciseResultRepository;
	private final ExerciseSessionRepository exerciseSessionRepository;
	private final AiUserProfileClient aiUserProfileClient;

	@Value("${ai.server.user-profile.batch-size:500}")
	private int batchSize;

	public void syncAllUserProfiles() {
		List<String> bodyParts = exerciseRepository.findDistinctBodyPartsByIsDeprecatedFalse();
		Long lastUserId = 0L;

		long totalTargetUsers = 0;
		List<AiUserProfileDto> allProfiles = new ArrayList<>();

		while (true) {
			List<Long> userIds = userRepository.findExerciseSessionUserIdsAfter(
				lastUserId, PageRequest.of(0, batchSize)
			);
			if (userIds.isEmpty()) {
				break;
			}
			lastUserId = userIds.getLast();
			totalTargetUsers += userIds.size();

			List<AiUserProfileDto> chunkProfiles = buildUserBatchProfiles(userIds, bodyParts);
			allProfiles.addAll(chunkProfiles);
		}

		if (allProfiles.isEmpty()) {
			log.info("사용자 프로필 배치 동기화 종료: targetUsers=0");
			return;
		}

		aiUserProfileClient.updateProfiles(new AiUserProfileSyncRequest(allProfiles));
		log.info("사용자 프로필 배치 동기화 종료: targetUsers={}, sentProfiles={}",
			totalTargetUsers, allProfiles.size());
	}

	private List<AiUserProfileDto> buildUserBatchProfiles(List<Long> userIds, List<String> bodyParts) {
		Map<Long, Map<String, Double>> bodyPartRatiosByUser = initializeBodyPartRatios(userIds, bodyParts);
		Map<Long, Map<String, Double>> typeRatiosByUser = initializeTypeRatios(userIds);
		Map<Long, Map<String, Double>> difficultyRatiosByUser = initializeDifficultyRatios(userIds);
		Map<Long, Double> weeklyFrequencyByUser = initializeWeeklyFrequency(userIds);

		List<ExerciseResultRepository.BodyPartRatioProjection> bodyPartRatios =
			exerciseResultRepository.findBodyPartRatiosByUserIds(userIds);
		bodyPartRatios.forEach(row ->
			bodyPartRatiosByUser.get(row.getUserId()).put(row.getBodyPart(), toDouble(row.getRatio()))
		);

		List<ExerciseResultRepository.ExerciseTypeRatioProjection> exerciseTypeRatios =
			exerciseResultRepository.findExerciseTypeRatiosByUserIds(userIds);
		exerciseTypeRatios.forEach(row ->
			typeRatiosByUser.get(row.getUserId()).put(row.getExerciseType(), toDouble(row.getRatio()))
		);

		List<ExerciseResultRepository.DifficultyRatioProjection> difficultyRatios =
			exerciseResultRepository.findDifficultyRatiosByUserIds(userIds);
		difficultyRatios.forEach(row ->
			difficultyRatiosByUser.get(
				row.getUserId()).put(String.valueOf(row.getDifficulty()),
				toDouble(row.getRatio())
			)
		);

		List<ExerciseSessionRepository.WeeklyFrequencyProjection> weeklyFrequencies =
			exerciseSessionRepository.findWeeklyFrequenciesByUserIds(userIds);
		weeklyFrequencies.forEach(
			row -> weeklyFrequencyByUser.put(row.getUserId(), toDouble(row.getWeeklyFrequency()))
		);

		List<AiUserProfileDto> profiles = new ArrayList<>(userIds.size());
		for (Long userId : userIds) {
			profiles.add(new AiUserProfileDto(
				userId,
				bodyPartRatiosByUser.get(userId),
				typeRatiosByUser.get(userId),
				difficultyRatiosByUser.get(userId),
				weeklyFrequencyByUser.get(userId)
			));
		}
		return profiles;
	}

	private Map<Long, Map<String, Double>> initializeBodyPartRatios(List<Long> userIds, List<String> bodyParts) {
		Map<Long, Map<String, Double>> result = new HashMap<>();
		for (Long userId : userIds) {
			Map<String, Double> ratios = new LinkedHashMap<>();
			bodyParts.forEach(bodyPart -> ratios.put(bodyPart, 0.0));
			result.put(userId, ratios);
		}
		return result;
	}

	private Map<Long, Map<String, Double>> initializeTypeRatios(List<Long> userIds) {
		Map<Long, Map<String, Double>> result = new HashMap<>();
		for (Long userId : userIds) {
			Map<String, Double> ratios = new LinkedHashMap<>();
			for (ExerciseType type : ExerciseType.values()) {
				ratios.put(type.name(), 0.0);
			}
			result.put(userId, ratios);
		}
		return result;
	}

	private Map<Long, Map<String, Double>> initializeDifficultyRatios(List<Long> userIds) {
		Map<Long, Map<String, Double>> result = new HashMap<>();
		for (Long userId : userIds) {
			Map<String, Double> ratios = new LinkedHashMap<>();
			DEFAULT_DIFFICULTIES.forEach(difficulty -> ratios.put(difficulty, 0.0));
			result.put(userId, ratios);
		}
		return result;
	}

	private Map<Long, Double> initializeWeeklyFrequency(List<Long> userIds) {
		Map<Long, Double> weeklyFrequencyByUser = new HashMap<>();
		userIds.forEach(userId -> weeklyFrequencyByUser.put(userId, 0.0));
		return weeklyFrequencyByUser;
	}

	private double toDouble(BigDecimal value) {
		return value == null ? 0.0 : value.doubleValue();
	}
}
