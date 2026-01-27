package com.raisedeveloper.server.domain.auth.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raisedeveloper.server.domain.auth.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	List<RefreshToken> findAllByUserId(Long userId);
}
