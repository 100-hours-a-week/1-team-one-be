package com.raisedeveloper.server.domain.push.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raisedeveloper.server.domain.auth.domain.FcmToken;
import com.raisedeveloper.server.domain.auth.infra.FcmTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FcmTokenTxService {

	private final FcmTokenRepository fcmTokenRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void markTokenUsed(FcmToken fcmToken) {
		fcmToken.used();
		fcmTokenRepository.save(fcmToken);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void revokeToken(FcmToken fcmToken) {
		fcmToken.revoke();
		fcmTokenRepository.save(fcmToken);
	}
}
