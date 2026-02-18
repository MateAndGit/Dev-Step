package com.mateandgit.devstep.domain.post.dto.request;

public record PostSearchCondition(
        String title,
        String content,
        String authorNickname
) {
}
