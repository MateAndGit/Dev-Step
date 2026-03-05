package com.mateandgit.devstep.domain.category.dto;

import com.mateandgit.devstep.domain.category.domain.Category;
import com.mateandgit.devstep.global.status.CategoryType;

public record CategoryResponse(
        CategoryType categoryType
) {
    public static CategoryResponse of(Category category) {
        return new CategoryResponse(category.getCategoryType());
    }
}
