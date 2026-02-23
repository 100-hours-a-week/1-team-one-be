package com.raisedeveloper.server.domain.post.infra;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raisedeveloper.server.domain.post.domain.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

	List<Tag> findByNameIn(List<String> names);
}
