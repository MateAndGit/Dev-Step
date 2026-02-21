package com.mateandgit.devstep.domain.postlike.controller;

import com.mateandgit.devstep.domain.postlike.service.PostLikeService;
import com.mateandgit.devstep.global.response.ApiResponse;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/post-likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        postLikeService.likePost(postId, userDetails);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{postId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelLikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        postLikeService.cancelLikePost(postId, userDetails);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
