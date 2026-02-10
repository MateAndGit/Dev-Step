package com.mateandgit.devstep.domain.user.controller;

import com.mateandgit.devstep.domain.user.dto.request.UserUpdateRequest;
import com.mateandgit.devstep.domain.user.dto.response.UserUpdateResponse;
import com.mateandgit.devstep.global.response.ApiResponse;
import com.mateandgit.devstep.domain.user.dto.request.UserCreateRequest;
import com.mateandgit.devstep.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping()
    public ResponseEntity<ApiResponse<Long>> register(@Valid @RequestBody final UserCreateRequest request) {
        return ResponseEntity.status(CREATED)
                .body(ApiResponse.success(userService.createUser(request)));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateUser(
            @PathVariable final Long userId,
            @Valid @RequestBody final UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(userId, request)));
    }
}
