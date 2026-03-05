package com.mateandgit.devstep.domain.post.dto.request;

import com.mateandgit.devstep.global.status.CategoryType;
import jakarta.validation.constraints.NotBlank;

public record PostUpdateRequest(
        @NotBlank(message = "categoryType is required")
        CategoryType categoryType,
        @NotBlank(message = "title is required")
        String title,
        @NotBlank(message = "content is required")
        String content
) {
}
