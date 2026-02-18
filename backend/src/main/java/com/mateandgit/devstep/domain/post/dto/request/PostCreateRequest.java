package com.mateandgit.devstep.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PostCreateRequest(
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Content is required")
        String content
) {
}
