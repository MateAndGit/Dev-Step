package com.mateandgit.devstep.domain.user.service;

import com.mateandgit.devstep.domain.user.dto.request.UserUpdateRequest;
import com.mateandgit.devstep.domain.user.dto.response.UserResponse;
import com.mateandgit.devstep.domain.user.dto.response.UserUpdateResponse;
import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.global.exception.ErrorCode;
import com.mateandgit.devstep.domain.user.dto.request.UserCreateRequest;
import com.mateandgit.devstep.domain.user.entity.User;
import com.mateandgit.devstep.domain.user.repository.UserRepository;
import com.mateandgit.devstep.global.status.UserStatus;
import com.mateandgit.devstep.global.utils.ValidationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.mateandgit.devstep.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Test
    @DisplayName("should Save User and Return ID When Creation is Successful")
    void createUser_success() {
        // given
        UserCreateRequest request = new UserCreateRequest("test", "test@test.com", "1234");
        User mockUser = User.createUser(request.nickname(), request.email(), request.password());
        setField(mockUser, "id", 1L);
        given(userRepository.save(any(User.class))).willReturn(mockUser);

        // when
        Long userId = userService.createUser(request);

        // then
        assertThat(userId).isEqualTo(1L);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
        assertThat(savedUser.getNickname()).isEqualTo("test");
    }


    @Test
    @DisplayName("should Throw BusinessException When Nickname Already Exists")
    void createUser_fail_duplicateNickname() {
        // given
        UserCreateRequest request = createUser("test", "test@test.com", "1234");
        given(userRepository.existsByNickname("test")).willReturn(true);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.createUser(request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(DUPLICATE_NICKNAME);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should Throw BusinessException When Email Already Exists")
    void createUser_fail_duplicateEmail() {
        // given
        UserCreateRequest request = createUser("test", "test@test.com", "1234");
        given(userRepository.existsByEmail("test@test.com")).willReturn(true);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.createUser(request);
        });

        assertThat(exception.getErrorCode()).isEqualTo(DUPLICATE_EMAIL);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should Throw BusinessException for Empty Nickname")
    void createUser_EmptyNickname() {
        // given
        UserCreateRequest request = createUser("", "test@test.com", "1234");

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", INVALID_NICKNAME_FORMAT);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should Throw BusinessException for Banned Nickname")
    void creteUser_BANNED_NICKNAME() {
        // given
        UserCreateRequest request = createUser("Banned_words1", "test@test.com", "1234");

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", BANNED_NICKNAME);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("should Throw BusinessException for Empty Email")
    void createUser_EmptyEmail() {
        // given
        UserCreateRequest request = createUser("test", "", "1234");

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", INVALID_EMAIL_FORMAT);
        verify(userRepository, never()).save(any(User.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress",
            "#@%^%#$@#$@#.com",
            "@example.com",
            "Joe Smith <email@example.com>",
            "email.example.com",
            "email@example@example.com"
    })
    @DisplayName("should Throw BusinessException for Invalid Email Formats")
    void validateEmail_Fail_InvalidFormat(String invalidEmail) {
        assertThatThrownBy(() -> ValidationUtils.validateEmail(invalidEmail))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_EMAIL_FORMAT.getMessage());
    }

    @Test
    @DisplayName("should Throw BusinessException When User Not Found")
    void updateUser_fail_whenUserNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        // when & then
        assertThrows(BusinessException.class, () -> {
            userService.updateUser(1L, UserUpdateRequest.builder().build());
        });
    }


    @Test
    @DisplayName("should Update Nickname and Email When Successful")
    void updateUser_success() {
        // given
        final Long userId = 1L;
        final User existingUser = new User("oldNickname", "old@email.com", "password123");
        setField(existingUser, "id", userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

        final String newNickname = "newNickname";
        final String newEmail = "new@email.com";
        given(userRepository.existsByNickname(newNickname)).willReturn(false);
        given(userRepository.existsByEmail(newEmail)).willReturn(false);

        final UserUpdateRequest updateRequest = new UserUpdateRequest(newNickname, newEmail);

        // when
        final UserUpdateResponse response = userService.updateUser(userId, updateRequest);

        // then
        assertThat(response.nickname()).isEqualTo(newNickname);
        assertThat(response.email()).isEqualTo(newEmail);

        // (더 중요) 실제 Mock 객체의 상태가 변경되었는지 검증
        assertThat(existingUser.getNickname()).isEqualTo(newNickname);
        assertThat(existingUser.getEmail()).isEqualTo(newEmail);
    }

    @Test
    @DisplayName("should Throw Exception When Nickname is Duplicated on Update")
    void updateUser_fail_whenNicknameIsDuplicated() {
        // given
        final Long userId = 1L;
        final User existingUser = new User("oldNickname", "old@email.com", "password123");
        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

        final String duplicatedNickname = "duplicatedNickname";
        given(userRepository.existsByNickname(duplicatedNickname)).willReturn(true);

        final UserUpdateRequest request = new UserUpdateRequest(duplicatedNickname, "new@email.com");

        // when & then
        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", DUPLICATE_NICKNAME);

        assertThat(existingUser.getNickname()).isEqualTo("oldNickname");
    }

    @Test
    @DisplayName("should Throw Exception When Email is Duplicated on Update")
    void updateUser_fail_whenEmailIsDuplicated() {
        // given
        final Long userId = 1L;
        final User existingUser = new User("oldNickname", "old@email.com", "password123");
        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

        final String newNickname = "newNickname";
        final String duplicatedEmail = "duplicated@email.com";
        given(userRepository.existsByEmail(duplicatedEmail)).willReturn(true);

        final UserUpdateRequest request = new UserUpdateRequest(newNickname, duplicatedEmail);

        // when & then
        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(BusinessException.class);

        assertThat(existingUser.getEmail()).isEqualTo("old@email.com");
    }

    @Test
    @DisplayName("should Throw BusinessException When User Not Found")
    void deleteUser_fail_whenUserNotFound() {
        // given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        // when & then
        assertThrows(BusinessException.class, () -> {
            userService.deleteUser(1L);
        });
    }

    @Test
    @DisplayName("should Update Nickname and Email When Successful")
    void deleteUser_success() {
        // given
        final Long userId = 1L;
        final User existingUser = new User("oldNickname", "old@email.com", "password123");
        setField(existingUser, "id", userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

        // when
        userService.deleteUser(userId);

        // then
        assertThat(existingUser.isDeleted()).isTrue();
        assertThat(existingUser.getStatus()).isEqualTo(UserStatus.DELETED);
    }

    @Test
    @DisplayName("Should return UserResponse when a user exists with the given ID")
    void getUser_success() {
        // given
        Long userId = 1L;
        User user = User.createUser("nick", "test@test.com", "password123");
        setField(user, "id", userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUser(userId);

        // then
        assertThat(response)
                .extracting("id", "nickname", "email")
                .containsExactly(userId, user.getNickname(), user.getEmail());
    }
    

    private UserCreateRequest createUser(String nickname, String email, String password) {
        return UserCreateRequest.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .build();
    }
}