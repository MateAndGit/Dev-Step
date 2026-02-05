package com.mateandgit.devstep.repository;

import com.mateandgit.devstep.entity.HealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {
}