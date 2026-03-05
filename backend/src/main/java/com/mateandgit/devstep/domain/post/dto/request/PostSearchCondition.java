package com.mateandgit.devstep.domain.post.dto.request;

public record PostSearchCondition(
        Long lastPostId,
        Long categoryId,
        String title,
        String content,
        String authorNickname
) {
}
