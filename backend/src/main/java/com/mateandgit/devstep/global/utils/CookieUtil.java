package com.mateandgit.devstep.global.utils;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public ResponseCookie createRefreshTokenCookie(String refreshToken, long maxAgeMs) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAgeMs / 1000)
                .sameSite("Strict")
                .build();
    }
}