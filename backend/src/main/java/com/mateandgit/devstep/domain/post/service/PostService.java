package com.mateandgit.devstep.domain.post.service;

import com.mateandgit.devstep.domain.category.domain.Category;
import com.mateandgit.devstep.domain.category.repository.CategoryRepository;
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
import com.mateandgit.devstep.global.exception.ErrorCode;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mateandgit.devstep.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Long createPost(CustomUserDetails userDetails, PostCreateRequest request) {

        User author = userRepository.findById(userDetails.user().getId())
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        Category category = categoryRepository.findByCategoryType(request.categoryType())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        Post post  = Post.createPost(category, request.title(), request.content(), author);
        Post savedPost = postRepository.save(post);

        return savedPost.getId();
    }

    public Slice<PostResponse> getPostList(Pageable pageable, PostSearchCondition condition) {
        return postRepository.searchGetPost(pageable, condition);
    }

    public PostResponse getPost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(POST_NOT_FOUND));

        return PostResponse.from(post);
    }

    @Transactional
    public PostUpdateResponse updatePost(Long postId, CustomUserDetails userDetails, PostUpdateRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(POST_NOT_FOUND));

        if (!post.getAuthor().getId().equals(userDetails.user().getId())) {
            throw new BusinessException(UNAUTHORIZED_ACCESS);
        }

        Category category = categoryRepository.findByCategoryType(request.categoryType())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        post.updatePost(category, request.title(), request.content());

        return PostUpdateResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId, CustomUserDetails userDetails) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(POST_NOT_FOUND));

        if (!post.getAuthor().getId().equals(userDetails.user().getId())) {
            throw new BusinessException(UNAUTHORIZED_ACCESS);
        }

        postRepository.delete(post);
    }
}
