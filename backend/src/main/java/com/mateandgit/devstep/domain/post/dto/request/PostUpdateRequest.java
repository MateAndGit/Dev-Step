package com.mateandgit.devstep.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PostUpdateRequest(
        @NotBlank(message = "title is required")
        String title,
        @NotBlank(message = "content is required")
        String content
) {
}
