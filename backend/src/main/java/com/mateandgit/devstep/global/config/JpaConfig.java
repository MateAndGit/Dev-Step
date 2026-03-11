package com.mateandgit.devstep.global.config;

import com.mateandgit.devstep.domain.auth.repository.RefreshTokenRepository;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.mateandgit.devstep.domain",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RefreshTokenRepository.class
        )
)
public class JpaConfig {
}