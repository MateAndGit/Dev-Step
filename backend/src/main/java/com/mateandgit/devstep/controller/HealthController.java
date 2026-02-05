package com.mateandgit.devstep.controller;

import com.mateandgit.devstep.entity.HealthCheck;
import com.mateandgit.devstep.repository.HealthCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final HealthCheckRepository repository;

    @GetMapping("/api/v1/health")
    public String healthCheck() {
        HealthCheck saved = repository.save(new HealthCheck("OK"));
        return "DB Connection Success! ID: " + saved.getId() + " at " + saved.getCheckedAt();
    }
}