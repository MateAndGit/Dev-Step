package com.mateandgit.devstep.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequest(
        @NotBlank(message = "content must not be blank")
        String content
) {
}
