package com.mateandgit.devstep.domain.comment.dto.response;

import com.mateandgit.devstep.domain.comment.entity.Comment;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CommentResponse(
        Long id,
        String content,
        String authorNickname,
        List<CommentResponse> replies,
        LocalDateTime createdAt
) {
    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorNickname(comment.getAuthor().getNickname())
                .createdAt(comment.getCreatedAt())
                .replies(comment.getChildComments().stream()
                        .map(CommentResponse::from)
                        .toList())
                .build();
    }
}
