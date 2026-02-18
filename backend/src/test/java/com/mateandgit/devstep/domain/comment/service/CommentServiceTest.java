package com.mateandgit.devstep.domain.comment.service;

import com.mateandgit.devstep.domain.comment.dto.request.CommentCreateRequest;
import com.mateandgit.devstep.domain.comment.entity.Comment;
import com.mateandgit.devstep.domain.comment.repository.CommentRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.mateandgit.devstep.domain.post.entity.QPost.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;


@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    public CommentService commentService;

    @Mock
    public CommentRepository commentRepository;

    @Mock
    public PostRepository postRepository;

    @Test
    @DisplayName("Should create a comment successfully when valid post and user are provided")
    void createComment_Success() {
        // given
        Long postId = 1L;
        User author = createMockUser(10L, "testUser");
        Post post = createMockPost(postId, author);

        CustomUserDetails userDetails = new CustomUserDetails(author);
        CommentCreateRequest request = new CommentCreateRequest("This is a comment");

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        Comment savedComment = Comment.createComment(request.content(), author, post);
        setField(savedComment, "id", 100L);
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        Long savedCommentId = commentService.createComment(postId, request, userDetails);

        // then
        assertThat(savedCommentId).isEqualTo(100L);
        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).save(any(Comment.class));
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