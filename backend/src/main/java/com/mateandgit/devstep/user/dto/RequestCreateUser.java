package com.mateandgit.devstep.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RequestCreateUser(
        @NotBlank(message = "nickname is required")
        String nickname,
        @NotBlank(message = "email is required")
        @Email(message = "this is not email format")
        String email,
        @NotBlank(message = "password is required")
        String password
) {
}
