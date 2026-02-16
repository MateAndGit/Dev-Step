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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mateandgit.devstep.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signUp(AuthSignUpRequest request) {
        if(userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(DUPLICATE_NICKNAME);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(DUPLICATE_EMAIL);
        }
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.createUser(request.nickname(), request.email(), encodedPassword);
        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }

    @Transactional
    public TokenResponse login(AuthLoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        tokenEntity -> tokenEntity.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(new RefreshToken(refreshToken, user.getId()))
                );

        return new TokenResponse(accessToken, refreshToken);
    }

    public String reissue(String refreshTokenRequest) {
        if (!jwtTokenProvider.validateToken(refreshTokenRequest)) {
            throw new BusinessException(INVALID_TOKEN);
        }
        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshTokenRequest)
                .orElseThrow(() -> new BusinessException(TOKEN_NOT_FOUND));

        User user = userRepository.findById(savedToken.getUserId())
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        return jwtTokenProvider.createAccessToken(user.getEmail());
    }

    @Transactional
    public void logout(CustomUserDetails userDetails) {
        Long userId = userDetails.user().getId();
        refreshTokenRepository.deleteByUserId(userId);
    }
}