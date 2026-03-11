package com.raisedeveloper.server.domain.exercise.event;

public final class ExerciseKafkaTopics {

	public static final String SESSION_COMPLETED = "exercise.session.completed";
	public static final String SESSION_REWARD_APPLIED = "exercise.session.reward-applied";
	public static final String ALARM_SESSION_CREATED_V1 = "exercise.session.created";
	public static final String ALARM_PUSH_RESULT_V1 = "exercise.session.push-result";

	private ExerciseKafkaTopics() {
	}
}
