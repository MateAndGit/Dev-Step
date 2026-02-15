package com.mateandgit.devstep.domain.user.service;

import com.mateandgit.devstep.domain.user.dto.request.UserCreateRequest;
import com.mateandgit.devstep.domain.user.dto.request.UserUpdateRequest;
import com.mateandgit.devstep.domain.user.dto.response.UserResponse;
import com.mateandgit.devstep.domain.user.dto.response.UserUpdateResponse;
import com.mateandgit.devstep.domain.user.entity.User;
import com.mateandgit.devstep.domain.user.repository.UserRepository;
import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.global.status.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.mateandgit.devstep.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("Create User: Success")
    void createUser_success() {
        // given
        UserCreateRequest request = new UserCreateRequest("tester", "test@example.com", "password123");
        User mockUser = User.createUser(request.nickname(), request.email(), request.password());
        setField(mockUser, "id", 1L);

        given(userRepository.existsByNickname(request.nickname())).willReturn(false);
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(mockUser);

        // when
        Long userId = userService.createUser(request);

        // then
        assertThat(userId).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Create User Fail: Duplicate Nickname")
    void createUser_fail_duplicateNickname() {
        // given
        UserCreateRequest request = new UserCreateRequest("duplicate", "test@example.com", "pw");
        given(userRepository.existsByNickname("duplicate")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DUPLICATE_NICKNAME);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Update User: Success")
    void updateUser_success() {
        // given
        Long userId = 1L;
        User user = User.createUser("oldNick", "old@email.com", "pw");
        setField(user, "id", userId);

        UserUpdateRequest updateRequest = new UserUpdateRequest("newNick", "new@email.com");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname("newNick")).willReturn(false);
        given(userRepository.existsByEmail("new@email.com")).willReturn(false);

        // when
        UserUpdateResponse response = userService.updateUser(userId, updateRequest);

        // then
        assertThat(response.nickname()).isEqualTo("newNick");
        assertThat(user.getNickname()).isEqualTo("newNick");
    }

    @Test
    @DisplayName("Delete User: Success (Soft Delete)")
    void deleteUser_success() {
        // given
        Long userId = 1L;
        User user = User.createUser("user", "user@example.com", "pw");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userService.deleteUser(userId);

        // then
        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
    }

    @Test
    @DisplayName("Get User: Success")
    void getUser_success() {
        // given
        Long userId = 1L;
        User user = User.createUser("tester", "test@example.com", "pw");
        setField(user, "id", userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUser(userId);

        // then
        assertThat(response.nickname()).isEqualTo("tester");
        assertThat(response.id()).isEqualTo(userId);
    }
}