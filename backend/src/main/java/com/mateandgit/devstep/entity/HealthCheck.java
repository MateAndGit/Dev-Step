package com.mateandgit.devstep.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

@Entity
@Getter
public class HealthCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;
    private LocalDateTime checkedAt;

    protected HealthCheck() {}

    public HealthCheck(String status) {
        this.status = status;
        this.checkedAt = LocalDateTime.now();
    }
}
