package com.mateandgit.devstep.domain.auth.controller;

import com.mateandgit.devstep.domain.auth.dto.TokenResponse;
import com.mateandgit.devstep.domain.auth.service.AuthService;
import com.mateandgit.devstep.domain.user.dto.request.UserCreateRequest;
import com.mateandgit.devstep.domain.user.dto.request.UserLoginRequest;
import com.mateandgit.devstep.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping()
    public ResponseEntity<ApiResponse<Long>> register(@Valid @RequestBody final UserCreateRequest request) {
        return ResponseEntity.status(CREATED)
                .body(ApiResponse.success(authService.createUser(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletResponse response) {
        TokenResponse tokenResponse = authService.login(request);
        response.addHeader(HttpHeaders.SET_COOKIE, tokenResponse.cookie().toString());
        return ResponseEntity.ok(ApiResponse.success(tokenResponse.accessToken()));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<String>> reissue(
            @CookieValue(value = "refreshToken") String refreshToken) {

        String newAccessToken = authService.reissue(refreshToken);

        return ResponseEntity.ok(ApiResponse.success(newAccessToken));
    }
}