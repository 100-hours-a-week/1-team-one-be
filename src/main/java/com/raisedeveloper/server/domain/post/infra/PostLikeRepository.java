package com.raisedeveloper.server.domain.post.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.raisedeveloper.server.domain.post.domain.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

	boolean existsByPostIdAndUserId(Long postId, Long userId);

	void deleteByPostIdAndUserId(Long postId, Long userId);

	@Query("select pl.post.id from PostLike pl where pl.user.id = :userId and pl.post.id in :postIds")
	List<Long> findPostIdsByUserIdAndPostIdIn(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

	@Query("""
		select pl.post.id as postId, count(pl.id) as likeCount
		from PostLike pl
		where pl.post.deletedAt is null
		group by pl.post.id
		""")
	List<PostLikeCount> findLikeCounts();

	interface PostLikeCount {
		Long getPostId();

		long getLikeCount();
	}
}
