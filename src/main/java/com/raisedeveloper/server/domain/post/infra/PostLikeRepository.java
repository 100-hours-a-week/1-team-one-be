package com.raisedeveloper.server.domain.post.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.domain.post.domain.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	boolean existsByPostIdAndUserId(Long postId, Long userId);

	@Modifying
	@Query(value = """
		insert ignore into post_likes (created_at, post_id, user_id)
		values (current_timestamp, :postId, :userId)
		""", nativeQuery = true)
	int insertIgnoreByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

	long deleteByPostIdAndUserId(Long postId, Long userId);

	@Query("select pl.post.id from PostLike pl where pl.user.id = :userId and pl.post.id in :postIds")
	List<Long> findPostIdsByUserIdAndPostIdIn(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

}
