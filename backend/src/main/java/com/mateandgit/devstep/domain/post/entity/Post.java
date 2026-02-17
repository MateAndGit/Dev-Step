package com.mateandgit.devstep.domain.post.entity;

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

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Post(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public static Post createPost(String title, String content, User author) {
        ValidationUtils.validatePostCreateRequest(title, content);
        return Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();
    }

    public static Post updatePost(String title, String content) {
        ValidationUtils.validatePostCreateRequest(title, content);
        return Post.builder()
                .title(title)
                .content(content)
                .build();
    }
}
