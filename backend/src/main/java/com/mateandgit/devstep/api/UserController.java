package com.mateandgit.devstep.api;

import com.mateandgit.devstep.global.response.ApiResponse;
import com.mateandgit.devstep.user.dto.RequestCreateUser;
import com.mateandgit.devstep.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("user/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Long>> register(@Valid @RequestBody RequestCreateUser request) {
        Long savedUser= userService.createUser(request);
        ApiResponse<Long> response = ApiResponse.success(savedUser);
        return ResponseEntity.status(CREATED).body(response);
    }


}
