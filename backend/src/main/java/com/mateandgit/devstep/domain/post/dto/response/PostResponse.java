package com.mateandgit.devstep.domain.post.dto.response;

import com.mateandgit.devstep.domain.comment.dto.response.CommentResponse;
import com.mateandgit.devstep.domain.post.entity.Post;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostResponse(
        Long id,
        String title,
        String content,
        String authorNickname,
        LocalDateTime createdAt,
        List<CommentResponse> comments,
        Long likeCount
) {


    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getAuthor().getNickname())
                .createdAt(post.getCreatedAt())
                .comments(post.getComments().stream()
                        .filter(comment -> comment.getParentComment() == null)
                        .map(CommentResponse::from)
                        .toList())
                .likeCount(post.getLikeCount())
                .build();
    }
}
