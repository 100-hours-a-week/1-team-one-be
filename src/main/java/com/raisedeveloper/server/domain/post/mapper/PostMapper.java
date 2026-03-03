package com.raisedeveloper.server.domain.post.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.raisedeveloper.server.domain.post.domain.Post;
import com.raisedeveloper.server.domain.post.domain.PostTag;
import com.raisedeveloper.server.domain.post.domain.Tag;
import com.raisedeveloper.server.domain.post.dto.PostAuthor;
import com.raisedeveloper.server.domain.post.dto.PostDetail;
import com.raisedeveloper.server.domain.post.dto.PostListItem;
import com.raisedeveloper.server.domain.post.dto.PostTagInfo;
import com.raisedeveloper.server.domain.user.domain.UserCharacter;
import com.raisedeveloper.server.domain.user.domain.UserProfile;

@Component
public class PostMapper {

	public PostAuthor toPostAuthor(Long authorId, UserProfile profile, UserCharacter character) {
		return new PostAuthor(
			authorId,
			profile.getImagePath(),
			profile.getNickname(),
			character.getLevel(),
			character.getStreak()
		);
	}

	public PostDetail toPostDetail(
		Post post,
		boolean isAuthor,
		PostAuthor author,
		List<String> images,
		List<PostTagInfo> tags,
		boolean isLiked,
		int likeCount
	) {
		return new PostDetail(
			post.getId(),
			isAuthor,
			author,
			post.getTitle(),
			post.getContent(),
			images,
			tags,
			likeCount,
			post.getCreatedAt(),
			isLiked
		);
	}

	public PostListItem toPostListItem(Post post, PostAuthor author, List<PostTagInfo> tags, boolean isLiked,
		int likeCount) {
		return new PostListItem(
			post.getId(),
			author,
			post.getTitle(),
			post.getContent(),
			post.getThumbnailImagePath(),
			tags,
			likeCount,
			post.getCreatedAt(),
			isLiked
		);
	}

	public PostTagInfo toTagInfo(Tag tag) {
		return new PostTagInfo(tag.getId(), tag.getName());
	}

	public PostTagInfo toTagInfo(PostTag postTag) {
		Tag tag = postTag.getTag();
		return new PostTagInfo(tag.getId(), tag.getName());
	}
}
