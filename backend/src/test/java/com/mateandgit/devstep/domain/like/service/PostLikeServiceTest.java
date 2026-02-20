package com.mateandgit.devstep.domain.like.service;

import com.mateandgit.devstep.domain.like.repositroy.PostLikeRepository;
import com.mateandgit.devstep.domain.post.entity.Post;
import com.mateandgit.devstep.domain.post.repository.PostRepository;
import com.mateandgit.devstep.domain.user.entity.User;
import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @InjectMocks
    private PostLikeService postLikeService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Test
    @DisplayName("Success: User likes a post for the first time")
    void likePost_Success() {
        // given
        User user = createMockUser(1L, "nickname");
        Post post = createMockPost(1L, user);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        given(postRepository.existsByIdAndAuthorId(post.getId(), user.getId())).willReturn(false);
        given(postRepository.findByIdWithLock(post.getId())).willReturn(Optional.of(post));

        // when
        postLikeService.likePost(post.getId(), userDetails);

        // then
        assertThat(post.getLikeCount()).isEqualTo(1);
        verify(postLikeRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Faliure: User cannot like the same post twice")
    void likePost_Fail_AlreadyLiked() {
        // given
        User user = createMockUser(1L, "nickname");
        Post post = createMockPost(1L, user);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        given(postRepository.existsByIdAndAuthorId(any(),any())).willReturn(true);
        
        // when & then
        assertThrows(BusinessException.class, () -> {
            postLikeService.likePost(post.getId(), userDetails);
        });
    }



    private User createMockUser(Long id, String nickname) {
        User user = User.createUser(nickname, nickname + "@test.com", "password");
        setField(user, "id", id);
        return user;
    }

    private Post createMockPost(Long id, User author) {
        Post post = Post.createPost("title", "content", author);
        setField(post, "id", id);
        return post;
    }
}