package com.mateandgit.devstep.domain.post.dto.response;

public record PostResponse(
        Long id,
        String title,
        String content,
        String authorNickname
) {
}
