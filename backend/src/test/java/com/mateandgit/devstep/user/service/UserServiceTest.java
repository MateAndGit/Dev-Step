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
    @DisplayName("회원 등록 성공: 유저 정보를 저장하고 생성된 ID를 반환한다")
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
    @DisplayName("이미 존재하는 닉네임으로 가입 시도 시 BusinessException이 발생한다")
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
    @DisplayName("이미 존재하는 이메일로 가입 시도시 BusinessException이 발생한다.")
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
    @DisplayName("회원 등록 시도시 닉네임을 입력하지 않으면 BusinessException이 발생한다")
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
    @DisplayName("회원 등록 시도시 금지된 닉네임을 입력하면 BusinessException이 발생한다")
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
    @DisplayName("회원 등록 시도시 이메일을 입력하지 않으면 BusinessException이 발생한다.")
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
            "plainaddress",            // @ 없음
            "#@%^%#$@#$@#.com",        // 특수문자 범벅
            "@example.com",            // 아이디 없음
            "Joe Smith <email@example.com>", // 이름 포함됨
            "email.example.com",       // @ 없음
            "email@example@example.com"// @ 중복
    })
    @DisplayName("잘못된 형식의 이메일은 예외가 발생한다")
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