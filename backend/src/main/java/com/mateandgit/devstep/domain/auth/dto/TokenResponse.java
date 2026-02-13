package com.mateandgit.devstep.domain.auth.dto;

import org.springframework.http.ResponseCookie;

public record TokenResponse(
        String accessToken,
        ResponseCookie cookie
) {}
