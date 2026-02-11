package com.raisedeveloper.server.domain.post.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.domain.post.domain.PostImage;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

	List<PostImage> findByPostIdOrderBySortOrderAsc(Long postId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("DELETE FROM PostImage pi WHERE pi.post.id = :postId")
	void deleteAllByPostId(@Param("postId") Long postId);
}
