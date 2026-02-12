package com.raisedeveloper.server.domain.user.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raisedeveloper.server.domain.user.domain.UserCharacter;

public interface UserCharacterRepository extends JpaRepository<UserCharacter, Long> {

	Optional<UserCharacter> findByUserId(Long userId);

	List<UserCharacter> findByUserIdIn(List<Long> userIds);
}
