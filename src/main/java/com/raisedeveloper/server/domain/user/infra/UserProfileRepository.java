package com.raisedeveloper.server.domain.user.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.user.domain.UserProfile;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

	boolean existsByNickname(String nickname);

	Optional<UserProfile> findByUserId(Long userId);

	boolean existsByNicknameAndUserIdNot(String nickname, Long userId);
}
