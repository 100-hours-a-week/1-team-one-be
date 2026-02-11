package com.raisedeveloper.server.domain.post.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raisedeveloper.server.domain.post.domain.PostImage;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

	List<PostImage> findByPostIdAndDeletedAtIsNull(Long postId);

	List<PostImage> findByPostIdAndDeletedAtIsNullOrderBySortOrderAsc(Long postId);
}
