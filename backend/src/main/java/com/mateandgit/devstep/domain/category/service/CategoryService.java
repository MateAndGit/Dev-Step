package com.mateandgit.devstep.domain.category.service;

import com.mateandgit.devstep.domain.category.dto.CategoryResponse;
import com.mateandgit.devstep.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::of)
                .toList();
    }
}
