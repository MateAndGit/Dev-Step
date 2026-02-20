package com.mateandgit.devstep.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        Long parentId,
        @NotBlank(message = "content must not be blank")
        String content
) {
}
