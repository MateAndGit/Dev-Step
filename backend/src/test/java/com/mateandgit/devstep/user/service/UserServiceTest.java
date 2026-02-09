package com.mateandgit.devstep.user.service;

import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.global.exception.ErrorCode;
import com.mateandgit.devstep.user.dto.RequestCreateUser;
import com.mateandgit.devstep.user.entity.User;
import com.mateandgit.devstep.user.repository.UserRepository;
import com.mateandgit.devstep.utils.ValidationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.mateandgit.devstep.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        RequestCreateUser request = new RequestCreateUser("test", "test@test.com", "1234");
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
        RequestCreateUser request = createUser("test", "test@test.com", "1234");
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
        RequestCreateUser request = createUser("test", "test@test.com", "1234");
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
        RequestCreateUser request = createUser("", "test@test.com", "1234");

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
        RequestCreateUser request = createUser("Banned_words1", "test@test.com", "1234");

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
        RequestCreateUser request = createUser("test", "", "1234");

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

    private RequestCreateUser createUser(String nickname, String email, String password) {
        return RequestCreateUser.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .build();
    }

}
