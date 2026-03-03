package com.raisedeveloper.server.domain.user.infra;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.raisedeveloper.server.domain.user.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	@Query(value = """
		SELECT DISTINCT es.user_id
		FROM exercise_sessions es
		JOIN users u ON u.id = es.user_id
		WHERE es.user_id > :lastUserId
		  AND u.deleted_at IS NULL
		ORDER BY es.user_id ASC
		""", nativeQuery = true)
	List<Long> findExerciseSessionUserIdsAfter(@Param("lastUserId") Long lastUserId, Pageable pageable);
}
