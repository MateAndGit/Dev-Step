package com.mateandgit.devstep.domain.comment.service;

import com.mateandgit.devstep.domain.comment.dto.request.CommentCreateRequest;
import com.mateandgit.devstep.domain.comment.entity.Comment;
import com.mateandgit.devstep.domain.comment.repository.CommentRepository;
import com.mateandgit.devstep.domain.post.entity.Post;
import com.mateandgit.devstep.domain.post.repository.PostRepository;
import com.mateandgit.devstep.domain.user.entity.User;
import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.global.exception.ErrorCode;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mateandgit.devstep.global.exception.ErrorCode.POST_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public Long createComment(Long postId, CommentCreateRequest request, CustomUserDetails userDetails) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(POST_NOT_FOUND));

        Comment parentComment = null;

        if (request.parentId() != null) {
            parentComment = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

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

        return savedComment.getId();
    }

}
