package com.mateandgit.devstep.domain.post.service;

import com.mateandgit.devstep.domain.post.dto.request.PostCreateRequest;
import com.mateandgit.devstep.domain.post.dto.request.PostSearchCondition;
import com.mateandgit.devstep.domain.post.dto.request.PostUpdateRequest;
import com.mateandgit.devstep.domain.post.dto.response.PostResponse;
import com.mateandgit.devstep.domain.post.dto.response.PostUpdateResponse;
import com.mateandgit.devstep.domain.post.entity.Post;
import com.mateandgit.devstep.domain.post.repository.PostRepository;
import com.mateandgit.devstep.domain.user.entity.User;
import com.mateandgit.devstep.domain.user.repository.UserRepository;
import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mateandgit.devstep.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createPost(CustomUserDetails userDetails, PostCreateRequest request) {

        User author = userRepository.findById(userDetails.user().getId())
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        Post post  = Post.createPost(request.title(), request.content(), author);
        Post savedPost = postRepository.save(post);

        return savedPost.getId();
    }

    public Page<PostResponse> getPostList(Pageable pageable, PostSearchCondition condition) {
        return postRepository.searchGetPost(pageable, condition);
    }

    public PostResponse getPost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(POST_NOT_FOUND));

        return PostResponse.from(post);
    }

    public PostUpdateResponse updatePost(Long postId, CustomUserDetails userDetails, PostUpdateRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(POST_NOT_FOUND));

        if (!post.getAuthor().getId().equals(userDetails.user().getId())) {
            throw new BusinessException(UNAUTHORIZED_ACCESS);
        }

        Post updatedPost = Post.updatePost(request.title(), request.content());

        return PostUpdateResponse.from(updatedPost);
    }
}
