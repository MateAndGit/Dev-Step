package com.mateandgit.devstep.domain.post.dto.response;

import com.mateandgit.devstep.domain.post.entity.Post;
import lombok.Builder;

@Builder
public record PostUpdateResponse(
        Long postId,
        String title,
        String content
) {
    public static PostUpdateResponse from(Post post) {
        return PostUpdateResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .build();
    }
}
