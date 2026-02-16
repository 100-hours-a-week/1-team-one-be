package com.raisedeveloper.server.domain.user.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.user.domain.UserProfile;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

	boolean existsByNickname(String nickname);

	Optional<UserProfile> findByUserId(Long userId);

	List<UserProfile> findByUserIdIn(List<Long> userIds);

	boolean existsByNicknameAndUserIdNot(String nickname, Long userId);
}
