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
import com.mateandgit.devstep.global.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    public PostService postService;

    @Mock
    public UserRepository userRepository;

    @Mock
    public PostRepository postRepository;

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_Success() {
        // given
        User author = User.createUser("nick", "test@test.com", "pw123");
        setField(author, "id", 1L);
        CustomUserDetails userDetails = new CustomUserDetails(author);
        given(userRepository.findById(1L)).willReturn(Optional.of(author));

        PostCreateRequest request = new PostCreateRequest("title", "content");
        Post savedPost = Post.createPost(request.title(), request.content(), author);
        setField(savedPost, "id", 100L);
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        // when
        Long postId = postService.createPost(userDetails, request);

        // then
        assertEquals(100L, postId);
        verify(postRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("게시글 목록 조회 - 성공")
    void getPostList_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        PostSearchCondition condition = new PostSearchCondition("title", "content", "author");

        List<PostResponse> content = List.of(
                new PostResponse(1L, "title1", "content1", "user1"),
                new PostResponse(2L, "title2", "content2", "user2")
        );
        Page<PostResponse> expectedPage = new PageImpl<>(content, pageable, content.size());

        given(postRepository.searchGetPost(pageable, condition)).willReturn(expectedPage);

        // when
        Page<PostResponse> result = postService.getPostList(pageable, condition);

        // then
        assertAll(
                () -> assertEquals(2, result.getContent().size()),
                () -> assertEquals("title1", result.getContent().get(0).title()),
                () -> verify(postRepository, times(1)).searchGetPost(pageable, condition)
        );
    }

    @Test
    @DisplayName("")
    void getPost_Success() {
        // given
        Long postId = 1L;
        User author = User.createUser("nick", "test@test.com", "pw123");
        Post post = Post.createPost("title", "content", author);
        setField(post, "id", postId);
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        PostResponse response = postService.getPost(postId);

        // then
        assertThat(response.title()).isEqualTo("title");
        assertThat(response.content()).isEqualTo("content");
        assertThat(response.authorNickname()).isEqualTo("nick");
    }

    @Test
    @DisplayName("")
    void updatePost_Success() {
        // given
        User author = User.createUser("nick", "test@test.com", "pw123");
        setField(author, "id", 1L);
        CustomUserDetails userDetails = new CustomUserDetails(author);

        Long postId = 1L;
        Post post = Post.createPost("title", "content", author);
        setField(post,"id", postId);
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        PostUpdateRequest request = new PostUpdateRequest("newTitle", "newContent");

        // when
        PostUpdateResponse response = postService.updatePost(postId, userDetails, request);

        // then
        assertThat(response.title()).isEqualTo("newTitle");
        assertThat(response.content()).isEqualTo("newContent");
    }

}