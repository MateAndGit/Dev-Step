package com.mateandgit.devstep.domain.post.controller;

import com.mateandgit.devstep.domain.post.dto.request.PostCreateRequest;
import com.mateandgit.devstep.domain.post.dto.request.PostSearchCondition;
import com.mateandgit.devstep.domain.post.dto.request.PostUpdateRequest;
import com.mateandgit.devstep.domain.post.dto.response.PostResponse;
import com.mateandgit.devstep.domain.post.dto.response.PostUpdateResponse;
import com.mateandgit.devstep.domain.post.service.PostService;
import com.mateandgit.devstep.global.response.ApiResponse;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostCreateRequest request) {
        Long postId = postService.createPost(userDetails, request);
        return ResponseEntity.ok(ApiResponse.success(postId));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPostList(
            Pageable pageable,
            @ModelAttribute PostSearchCondition condition) {;
        Page<PostResponse> postList = postService.getPostList(pageable, condition);
        return ResponseEntity.ok(ApiResponse.success(postList));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long postId) {
        PostResponse postResponse = postService.getPost(postId);
        return ResponseEntity.ok(ApiResponse.success(postResponse));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostUpdateResponse>> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PostUpdateRequest request) {
        PostUpdateResponse postUpdateResponse = postService.updatePost(postId, userDetails, request);
        return ResponseEntity.ok(ApiResponse.success(postUpdateResponse));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        postService.deletePost(postId, userDetails);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
