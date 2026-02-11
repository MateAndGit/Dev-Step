package com.mateandgit.devstep.domain.user.controller;

import com.mateandgit.devstep.domain.user.dto.request.UserCreateRequest;
import com.mateandgit.devstep.domain.user.dto.request.UserSearchCondition;
import com.mateandgit.devstep.domain.user.dto.request.UserUpdateRequest;
import com.mateandgit.devstep.domain.user.dto.response.UserResponse;
import com.mateandgit.devstep.domain.user.dto.response.UserUpdateResponse;
import com.mateandgit.devstep.domain.user.service.UserService;
import com.mateandgit.devstep.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

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

    @GetMapping("/search/{adminId}")
        public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUser(
            @PathVariable final Long adminId,
            @ModelAttribute @Valid final UserSearchCondition condition,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserList(adminId,pageable, condition)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable final Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUser(userId)));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserUpdateResponse>> updateUser(
            @PathVariable final Long userId,
            @Valid @RequestBody final UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(userId, request)));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Long>> deleteUser(@PathVariable final Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.deleteUser(userId)));
    }
}
