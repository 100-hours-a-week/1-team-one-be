package com.raisedeveloper.server.domain.auth.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raisedeveloper.server.domain.auth.domain.FcmToken;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

	Optional<FcmToken> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
