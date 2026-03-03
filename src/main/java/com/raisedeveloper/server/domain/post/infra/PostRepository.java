package com.raisedeveloper.server.domain.post.infra;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.domain.post.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	@Query("""
		SELECT p FROM Post p
		JOIN FETCH p.user
		ORDER BY p.createdAt DESC, p.id DESC
		""")
	List<Post> findPage(Pageable pageable);

	@Query("""
		SELECT DISTINCT p FROM Post p
		JOIN FETCH p.user
		JOIN PostTag pt ON pt.post = p
		JOIN pt.tag t
		WHERE t.name IN :tagNames
		ORDER BY p.createdAt DESC, p.id DESC
		""")
	List<Post> findPageByTagNames(@Param("tagNames") List<String> tagNames, Pageable pageable);

	@Query("""
		SELECT p FROM Post p
		JOIN FETCH p.user
		WHERE (p.createdAt < :createdAt OR (p.createdAt = :createdAt AND p.id < :id))
		ORDER BY p.createdAt DESC, p.id DESC
		""")
	List<Post> findPageByCursor(
		@Param("createdAt") LocalDateTime createdAt,
		@Param("id") Long id,
		Pageable pageable
	);

	@Query("""
		SELECT DISTINCT p FROM Post p
		JOIN FETCH p.user
		JOIN PostTag pt ON pt.post = p
		JOIN pt.tag t
		WHERE t.name IN :tagNames
			AND (p.createdAt < :createdAt OR (p.createdAt = :createdAt AND p.id < :id))
		ORDER BY p.createdAt DESC, p.id DESC
		""")
	List<Post> findPageByTagNamesAndCursor(
		@Param("tagNames") List<String> tagNames,
		@Param("createdAt") LocalDateTime createdAt,
		@Param("id") Long id,
		Pageable pageable
	);

	@Query("""
		SELECT p FROM Post p
		WHERE p.user.id = :authorId
		ORDER BY p.createdAt DESC, p.id DESC
		""")
	List<Post> findPageByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

	@Query("""
		SELECT DISTINCT p FROM Post p
		JOIN PostTag pt ON pt.post = p
		JOIN pt.tag t
		WHERE p.user.id = :authorId
			AND t.name IN :tagNames
		ORDER BY p.createdAt DESC, p.id DESC
		""")
	List<Post> findPageByAuthorIdAndTagNames(
		@Param("authorId") Long authorId,
		@Param("tagNames") List<String> tagNames,
		Pageable pageable
	);

	@Query("""
		SELECT p FROM Post p
		WHERE p.user.id = :authorId
			AND (p.createdAt < :createdAt OR (p.createdAt = :createdAt AND p.id < :id))
		ORDER BY p.createdAt DESC, p.id DESC
		""")
	List<Post> findPageByAuthorIdAndCursor(
		@Param("authorId") Long authorId,
		@Param("createdAt") LocalDateTime createdAt,
		@Param("id") Long id,
		Pageable pageable
	);

	@Query("""
		SELECT DISTINCT p FROM Post p
		JOIN PostTag pt ON pt.post = p
		JOIN pt.tag t
		WHERE p.user.id = :authorId
			AND t.name IN :tagNames
			AND (p.createdAt < :createdAt OR (p.createdAt = :createdAt AND p.id < :id))
		ORDER BY p.createdAt DESC, p.id DESC
		""")
	List<Post> findPageByAuthorIdAndTagNamesAndCursor(
		@Param("authorId") Long authorId,
		@Param("tagNames") List<String> tagNames,
		@Param("createdAt") LocalDateTime createdAt,
		@Param("id") Long id,
		Pageable pageable
	);
}
