package com.mateandgit.devstep.domain.comment.service;

import com.mateandgit.devstep.domain.comment.dto.request.CommentCreateRequest;
import com.mateandgit.devstep.domain.comment.entity.Comment;
import com.mateandgit.devstep.domain.comment.repository.CommentRepository;
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

import static com.mateandgit.devstep.global.exception.ErrorCode.INVALID_COMMENT_DEPTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
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
        // request의 parentId는 null (일반 댓글)
        CommentCreateRequest request = new CommentCreateRequest(null, "This is a comment");

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // 엔티티 생성 메서드에도 null(부모 없음)을 명시
        Comment savedComment = Comment.createComment(request.content(), author, post, null);
        setField(savedComment, "id", 100L);
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        Long savedCommentId = commentService.createComment(postId, request, userDetails);

        // then
        assertThat(savedCommentId).isEqualTo(100L);
        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 작성 실패: 대댓글에 다시 대댓글을 달려고 하면 예외가 발생한다")
    void createComment_Fail_InvalidDepth() {
        // given
        Long postId = 1L;
        User author = createMockUser(10L, "testUser");
        Post post = createMockPost(postId, author);

        Comment grandParent = createMockComment(99L, "할아버지 댓글", author, post, null);
        Comment parent = createMockComment(100L, "아버지 댓글(대댓글)", author, post, grandParent);

        CustomUserDetails userDetails = new CustomUserDetails(author);
        CommentCreateRequest request = new CommentCreateRequest(100L,"손자 댓글 시도");

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(100L)).willReturn(Optional.of(parent));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(postId, request, userDetails))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", INVALID_COMMENT_DEPTH);

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 수정 성공: 작성자가 본인의 댓글을 수정하면 내용이 변경된다")
    void updateComment_Success() {
        // given
        Long postId = 1L;
        Long commentId = 100L;
        User author = createMockUser(1L, "nickname");
        Post post = createMockPost(postId, author);

        CustomUserDetails userDetails = new CustomUserDetails(author);

        Comment existingComment = createMockComment(commentId, "old content", author, post, null);

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(existingComment));

        CommentCreateRequest request = new CommentCreateRequest(null, "updated content");

        // when
        commentService.updateComment(postId, commentId, request, userDetails);

        // then
        assertThat(existingComment.getContent()).isEqualTo("updated content");
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("")
    void deleteComment_Success() {
        // given
        Long postId = 1L;
        Long commentId = 100L;
        User author = createMockUser(1L, "nickname");
        Post post = createMockPost(postId, author);

        CustomUserDetails userDetails = new CustomUserDetails(author);

        Comment existingComment = createMockComment(commentId, "old content", author, post, null);

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(existingComment));

        // when
        commentService.deleteComment(postId, commentId, userDetails);

        // then
        verify(commentRepository, times(1)).delete(existingComment);
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

    private Comment createMockComment(Long id, String content, User author, Post post, Comment parent) {
        Comment comment = Comment.createComment(content, author, post, parent);
        setField(comment, "id", id);
        return comment;
    }

}