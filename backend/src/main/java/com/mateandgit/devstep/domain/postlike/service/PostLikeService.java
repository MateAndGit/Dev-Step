package com.mateandgit.devstep.domain.postlike.service;

import com.mateandgit.devstep.domain.postlike.entity.PostLike;
import com.mateandgit.devstep.domain.postlike.repositroy.PostLikeRepository;
import com.mateandgit.devstep.domain.post.entity.Post;
import com.mateandgit.devstep.domain.post.repository.PostRepository;
import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mateandgit.devstep.global.exception.ErrorCode.POST_ALREADY_LIKED;
import static com.mateandgit.devstep.global.exception.ErrorCode.POST_NOT_FOUND;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    public void likePost(Long postId , CustomUserDetails userDetails) {

        Long userId = userDetails.user().getId();

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
                throw new BusinessException(POST_ALREADY_LIKED);
        }

        Post post = postRepository.findByIdWithLock(postId)
                .orElseThrow(() -> new BusinessException(POST_NOT_FOUND));

        log.info("[LikeService] likePost execution - postId: {}, userId: {}", postId, userId);

        post.increaseLikeCount();
        PostLike like = PostLike.create(userDetails.user(), post);

        postLikeRepository.save(like);
    }
}
