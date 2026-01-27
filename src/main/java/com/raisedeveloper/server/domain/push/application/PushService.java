package com.raisedeveloper.server.domain.push.application;

import com.raisedeveloper.server.domain.exercise.domain.ExerciseSession;
import com.raisedeveloper.server.domain.user.domain.User;

public interface PushService {

	void sendSessionPush(User user, ExerciseSession session);
}
