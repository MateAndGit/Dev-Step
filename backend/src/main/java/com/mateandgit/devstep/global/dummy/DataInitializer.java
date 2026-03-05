package com.mateandgit.devstep.global.dummy;

import com.mateandgit.devstep.domain.category.domain.Category;
import com.mateandgit.devstep.domain.category.repository.CategoryRepository;
import com.mateandgit.devstep.global.status.CategoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            Arrays.stream(CategoryType.values()).forEach(categoryType -> {
                Category category = Category.builder()
                        .categoryType(categoryType)
                        .build();
                categoryRepository.save(category);
            });
        }
    }
}
