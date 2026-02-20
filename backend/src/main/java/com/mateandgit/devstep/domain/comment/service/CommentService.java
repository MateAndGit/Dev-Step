package com.mateandgit.devstep.domain.comment.service;

import com.mateandgit.devstep.domain.comment.dto.request.CommentCreateRequest;
import com.mateandgit.devstep.domain.comment.dto.request.CommentUpdateRequest;
import com.mateandgit.devstep.domain.comment.entity.Comment;
import com.mateandgit.devstep.domain.comment.repository.CommentRepository;
import com.mateandgit.devstep.domain.post.entity.Post;
import com.mateandgit.devstep.domain.post.repository.PostRepository;
import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.global.exception.ErrorCode;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mateandgit.devstep.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public Long createComment(Long postId, CommentCreateRequest request, CustomUserDetails userDetails) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(POST_NOT_FOUND));

        Comment parentComment = null;

        if (request.parentId() != null) {
            parentComment = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(COMMENT_NOT_FOUND));

            if (parentComment.getParentComment() != null) {
                throw new BusinessException(ErrorCode.INVALID_COMMENT_DEPTH);
            }
        }

        Comment comment = Comment.createComment(
                request.content(),
                userDetails.user(),
                post,
                parentComment
        );

        Comment savedComment = commentRepository.save(comment);
        post.addComment(savedComment);

        return savedComment.getId();
    }

    public void updateComment(Long postId, Long commentId, CommentUpdateRequest request, CustomUserDetails userDetails) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(POST_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(COMMENT_NOT_FOUND));

        if (!comment.getPost().getId().equals(post.getId())){
            throw new BusinessException(POST_NOT_FOUND);
        }

        if (!comment.getAuthor().getId().equals(userDetails.user().getId())) {
            throw new BusinessException(UNAUTHORIZED_ACCESS);
        }

        comment.updateContent(request.content());
    }

    public void deleteComment(Long postId, Long commentId, CustomUserDetails userDetails) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(POST_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(COMMENT_NOT_FOUND));

        if (!comment.getPost().getId().equals(post.getId())){
            throw new BusinessException(POST_NOT_FOUND);
        }

        if (!comment.getAuthor().getId().equals(userDetails.user().getId())) {
            throw new BusinessException(UNAUTHORIZED_ACCESS);
        }

        commentRepository.delete(comment);
    }
}
