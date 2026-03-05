package com.mateandgit.devstep.domain.category.repository;

import com.mateandgit.devstep.domain.category.domain.Category;
import com.mateandgit.devstep.global.status.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategoryType(CategoryType categoryType);
}
