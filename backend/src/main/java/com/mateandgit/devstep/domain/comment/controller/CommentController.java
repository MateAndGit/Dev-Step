package com.mateandgit.devstep.domain.comment.controller;

import com.mateandgit.devstep.domain.comment.dto.request.CommentCreateRequest;
import com.mateandgit.devstep.domain.comment.dto.request.CommentUpdateRequest;
import com.mateandgit.devstep.domain.comment.service.CommentService;
import com.mateandgit.devstep.global.response.ApiResponse;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<Long>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long savedCommentId = commentService.createComment(postId, request, userDetails);
        return ResponseEntity.ok(ApiResponse.success(savedCommentId));
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.updateComment(postId, commentId, request, userDetails);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.deleteComment(postId, commentId, userDetails);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
