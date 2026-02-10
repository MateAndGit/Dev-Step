package com.mateandgit.devstep.domain.user.entity;

import com.mateandgit.devstep.global.utils.ValidationUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public User(final String nickname, final String email, final String password) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }

    public static User createUser(final String nickname, final String email, final String password) {
        ValidationUtils.validateNickname(nickname);
        ValidationUtils.validateEmail(email);

        return User.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .build();
    }

    public void update(final String nickname, final String email) {
        ValidationUtils.validateNickname(nickname);
        ValidationUtils.validateEmail(email);

        this.nickname = nickname;
        this.email = email;
    }
}