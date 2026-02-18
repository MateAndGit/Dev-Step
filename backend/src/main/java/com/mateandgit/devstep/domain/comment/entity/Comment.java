package com.mateandgit.devstep.domain.comment.entity;

import com.mateandgit.devstep.domain.post.entity.Post;
import com.mateandgit.devstep.domain.user.entity.User;
import com.mateandgit.devstep.global.utils.ValidationUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<Comment> childComments = new ArrayList<>();

    public void addReply(Comment child) {
        this.childComments.add(child);
        child.setParent(this);
    }

    private void setParent(Comment parent) {
        this.parentComment = parent;
    }

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Comment(String content, User author, Post post, Comment parentComment) {
        this.content = content;
        this.author = author;
        this.post = post;
        this.parentComment = parentComment;
    }

    public static Comment createComment(String content, User author, Post post, Comment parentComment) {
        ValidationUtils.validateCommentCreateRequest(content);

        return Comment.builder()
                .content(content)
                .author(author)
                .post(post)
                .parentComment(parentComment)
                .build();
    }
}
