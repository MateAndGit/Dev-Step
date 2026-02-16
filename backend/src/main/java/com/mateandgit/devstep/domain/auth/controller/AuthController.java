package com.mateandgit.devstep.domain.auth.controller;

import com.mateandgit.devstep.domain.auth.dto.request.AuthLoginRequest;
import com.mateandgit.devstep.domain.auth.dto.request.AuthSignUpRequest;
import com.mateandgit.devstep.domain.auth.dto.response.TokenResponse;
import com.mateandgit.devstep.domain.auth.service.AuthService;
import com.mateandgit.devstep.global.response.ApiResponse;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import com.mateandgit.devstep.global.utils.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/signUp")
    public ResponseEntity<ApiResponse<Long>> signUp(@Valid @RequestBody final AuthSignUpRequest request) {
        return ResponseEntity.status(CREATED)
                .body(ApiResponse.success(authService.signUp(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody AuthLoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);

        ResponseCookie cookie = cookieUtil.createRefreshTokenCookie(tokenResponse.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(tokenResponse.accessToken()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<String>> reissue(
            @CookieValue(value = "refreshToken") String refreshToken) {

        String newAccessToken = authService.reissue(refreshToken);

        return ResponseEntity.ok(ApiResponse.success(newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails);
        ResponseCookie emptyCookie = cookieUtil.createEmptyRefreshTokenCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, emptyCookie.toString())
                .body(ApiResponse.success(null));
    }
}