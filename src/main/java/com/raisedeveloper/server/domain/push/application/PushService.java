package com.raisedeveloper.server.domain.push.application;

import java.util.concurrent.CompletableFuture;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.user.domain.User;

public interface PushService {

	PushDeliveryStatus sendSessionPush(User user, ExerciseSession session);

	CompletableFuture<PushDeliveryStatus> sendSessionPushAsync(User user, ExerciseSession session);
}
