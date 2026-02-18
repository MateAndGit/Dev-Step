package com.mateandgit.devstep.domain.post.dto.response;

import com.mateandgit.devstep.domain.post.entity.Post;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PostResponse(
        Long id,
        String title,
        String content,
        String authorNickname,
        LocalDateTime createdAt
) {
    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getAuthor().getNickname())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
