package com.raisedeveloper.server.domain.post.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raisedeveloper.server.domain.post.domain.PostTag;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
}
