package com.raisedeveloper.server.domain.post.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raisedeveloper.server.domain.post.domain.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

	Optional<Post> findByIdAndDeletedAtIsNull(Long id);
}
