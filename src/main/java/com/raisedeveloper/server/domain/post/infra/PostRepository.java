package com.raisedeveloper.server.domain.post.infra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.domain.post.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	Optional<Post> findByIdAndDeletedAtIsNull(Long id);

	@Query("""
		SELECT p FROM Post p
		JOIN FETCH p.user
		WHERE p.deletedAt IS NULL
		ORDER BY p.createdAt DESC, p.id DESC
		""")
	List<Post> findPage(Pageable pageable);

	@Query("""
		SELECT p FROM Post p
		JOIN FETCH p.user
		WHERE p.deletedAt IS NULL
			AND (p.createdAt < :createdAt OR (p.createdAt = :createdAt AND p.id < :id))
		ORDER BY p.createdAt DESC, p.id DESC
		""")
	List<Post> findPageByCursor(
		@Param("createdAt") LocalDateTime createdAt,
		@Param("id") Long id,
		Pageable pageable
	);

	@Query("""
		SELECT p FROM Post p
		WHERE p.deletedAt IS NULL
			AND p.user.id = :authorId
		ORDER BY p.createdAt DESC, p.id DESC
		""")
	List<Post> findPageByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

	@Query("""
		SELECT p FROM Post p
		WHERE p.deletedAt IS NULL
			AND p.user.id = :authorId
			AND (p.createdAt < :createdAt OR (p.createdAt = :createdAt AND p.id < :id))
		ORDER BY p.createdAt DESC, p.id DESC
		""")
	List<Post> findPageByAuthorIdAndCursor(
		@Param("authorId") Long authorId,
		@Param("createdAt") LocalDateTime createdAt,
		@Param("id") Long id,
		Pageable pageable
	);

	@Modifying
	@Query("""
		UPDATE Post p
		SET p.likeCount = case
			when (p.likeCount + :delta) < 0 then 0
			else (p.likeCount + :delta)
		END
		WHERE p.id = :postId
			AND p.deletedAt IS NULL
		""")
	void updateLikeCountDelta(@Param("postId") Long postId, @Param("delta") long delta);
}
