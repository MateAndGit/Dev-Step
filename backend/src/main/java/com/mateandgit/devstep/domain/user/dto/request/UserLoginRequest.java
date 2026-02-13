package com.mateandgit.devstep.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank(message = "email is required")
        @Email(message = "this is not email format")
        String email,
        @NotBlank(message = "password is required")
        String password
) {

}
