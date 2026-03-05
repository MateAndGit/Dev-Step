package com.mateandgit.devstep.domain.post.dto.request;

import com.mateandgit.devstep.global.status.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostCreateRequest(
        @NotNull(message = "Category is required")
        CategoryType categoryType,
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Content is required")
        String content
) {
}
