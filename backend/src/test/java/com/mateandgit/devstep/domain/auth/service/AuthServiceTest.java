package com.mateandgit.devstep.domain.auth.service;

import com.mateandgit.devstep.domain.auth.dto.request.AuthLoginRequest;
import com.mateandgit.devstep.domain.auth.dto.request.AuthSignUpRequest;
import com.mateandgit.devstep.domain.auth.dto.response.TokenResponse;
import com.mateandgit.devstep.domain.auth.entity.RefreshToken;
import com.mateandgit.devstep.domain.auth.repository.RefreshTokenRepository;
import com.mateandgit.devstep.domain.user.entity.User;
import com.mateandgit.devstep.domain.user.repository.UserRepository;
import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import com.mateandgit.devstep.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.mateandgit.devstep.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    public AuthService authService;

    @Mock
    public UserRepository userRepository;

    @Mock
    public PasswordEncoder passwordEncoder;

    @Mock
    public JwtTokenProvider jwtTokenProvider;

    @Mock
    public RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("Successfully sign up a new user")
    void signUp_Success() {
        // given
        AuthSignUpRequest request = new AuthSignUpRequest("nick", "test@test.com", "pw123");
        User user = User.createUser(request.nickname(), request.email(), request.password());
        setField(user, "id", 1L);

        given(userRepository.existsByEmail(any())).willReturn(false);
        given(userRepository.existsByNickname(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded_pw");
        given(userRepository.save(any())).willReturn(user);

        // when
        Long resultId = authService.signUp(request);

        // then
        assertThat(resultId).isNotNull();
        verify(userRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Fail sign up when nickname is already taken")
    void signUp_Fail_DuplicateNickname() {
        // given
        AuthSignUpRequest request = new AuthSignUpRequest("nick", "test@test.com", "pw123");
        given(userRepository.existsByNickname(any())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(DUPLICATE_NICKNAME.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Fail sign up when email is already registered")
    void signUp_Fail_DuplicateEmail() {
        // given
        AuthSignUpRequest request = new AuthSignUpRequest("nick", "test@test.com", "pw123");
        given(userRepository.existsByEmail(any())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(DUPLICATE_EMAIL.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Successfully login and return tokens")
    void login_Success() {
        // given
        AuthLoginRequest request = new AuthLoginRequest("test@test.com", "pw123");
        User user = User.createUser("nick", "test@test.com", "encoded_pw");
        setField(user, "id", 1L);

        given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);
        given(jwtTokenProvider.createAccessToken(any())).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(any())).willReturn("refresh-token");
        given(refreshTokenRepository.findByUserId(any())).willReturn(Optional.empty());

        // when
        TokenResponse response = authService.login(request);

        // then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        verify(refreshTokenRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Fail login when user email does not exist")
    void login_Fail_UserNotFound() {
        // given
        AuthLoginRequest request = new AuthLoginRequest("test@test.com", "pw123");
        given(userRepository.findByEmail(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Fail login when password does not match")
    void login_Fail_InvalidPassword() {
        // given
        AuthLoginRequest request = new AuthLoginRequest("test@test.com", "wrong_pw");
        User user = User.createUser("nick", "test@test.com", "encoded_pw");

        given(userRepository.findByEmail(any())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(any(), any())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(INVALID_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("Successfully reissue access token using valid refresh token")
    void reissue_Success() {
        // given
        String refreshToken = "valid-refresh-token";
        RefreshToken savedToken = new RefreshToken(refreshToken, 1L);
        User user = User.createUser("nick", "test@test.com", "pw123");
        setField(user, "id", 1L);

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.of(savedToken));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(jwtTokenProvider.createAccessToken(user.getEmail())).willReturn("new-access-token");

        // when
        String newAccessToken = authService.reissue(refreshToken);

        // then
        assertThat(newAccessToken).isEqualTo("new-access-token");
        verify(jwtTokenProvider, times(1)).createAccessToken(any());
    }

    @Test
    @DisplayName("Fail reissue when refresh token is invalid")
    void reissue_Fail_InvalidToken() {
        // given
        String invalidToken = "invalid-token";
        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.reissue(invalidToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(INVALID_TOKEN.getMessage());
    }

    @Test
    @DisplayName("Fail reissue when token is not found in database")
    void reissue_Fail_TokenNotFound() {
        // given
        String refreshToken = "valid-but-not-saved-token";
        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(TOKEN_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Fail reissue when user associated with token is not found")
    void reissue_Fail_UserNotFound() {
        // given
        String refreshToken = "valid-token";
        RefreshToken savedToken = new RefreshToken(refreshToken, 1L);

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.of(savedToken));
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Successfully logout by deleting the user's refresh token")
    void logout_Success() {
        // given
        Long userId = 1L;
        User user = User.createUser("nick", "test@test.com", "pw123");
        setField(user, "id", userId);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        // when
        authService.logout(userDetails);

        // then
        verify(refreshTokenRepository, times(1)).deleteByUserId(userId);
    }
}