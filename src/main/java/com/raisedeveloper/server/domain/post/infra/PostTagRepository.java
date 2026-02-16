package com.raisedeveloper.server.domain.post.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.domain.post.domain.PostTag;
import com.raisedeveloper.server.domain.post.domain.Tag;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

	void deleteAllByPostId(Long postId);

	@Query("SELECT pt.tag.name FROM PostTag pt WHERE pt.post.id = :postId")
	List<String> findTagNamesByPostId(@Param("postId") Long postId);

	@Query("SELECT pt.tag FROM PostTag pt WHERE pt.post.id = :postId")
	List<Tag> findTagsByPostId(@Param("postId") Long postId);

	@Query("""
		SELECT pt FROM PostTag pt
		JOIN FETCH pt.tag
		WHERE pt.post.id IN :postIds
		ORDER BY pt.post.id DESC, pt.id ASC
		""")
	List<PostTag> findByPostIdIn(@Param("postIds") List<Long> postIds);
}
