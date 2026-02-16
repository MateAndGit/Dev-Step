package com.mateandgit.devstep.domain.auth.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
