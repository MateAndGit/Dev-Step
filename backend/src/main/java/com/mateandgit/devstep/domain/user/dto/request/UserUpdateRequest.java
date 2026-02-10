package com.mateandgit.devstep.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserUpdateRequest(
        @NotBlank(message = "nickname is required")
        String nickname,
        @NotBlank(message = "email is required")
        @Email(message = "this is not email format")
        String email
) {
}
