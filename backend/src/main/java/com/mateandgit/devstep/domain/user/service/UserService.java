package com.mateandgit.devstep.domain.user.service;

import com.mateandgit.devstep.domain.user.dto.request.UserCreateRequest;
import com.mateandgit.devstep.domain.user.dto.request.UserUpdateRequest;
import com.mateandgit.devstep.domain.user.dto.response.UserUpdateResponse;
import com.mateandgit.devstep.domain.user.entity.User;
import com.mateandgit.devstep.domain.user.repository.UserRepository;
import com.mateandgit.devstep.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mateandgit.devstep.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public Long createUser(UserCreateRequest request) {

        if(userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(DUPLICATE_NICKNAME);
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(DUPLICATE_EMAIL);
        }

        User user = User.createUser(request.nickname(), request.email(), request.password());
        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }

    public UserUpdateResponse updateUser(Long userId, UserUpdateRequest request) {

        // TODO: Implement security check
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        validateForUpdate(user, request);
        user.update(request.nickname(), request.email());

        return UserUpdateResponse.from(user);
    }

    private void validateForUpdate(final User user, final UserUpdateRequest request) {
        validateNicknameForUpdate(user, request.nickname());
        validateEmailForUpdate(user, request.email());
    }

    private void validateNicknameForUpdate(final User user, final String newNickname) {
        if (user.getNickname().equals(newNickname)) {
            return;
        }
        if (userRepository.existsByNickname(newNickname)) {
            throw new BusinessException(DUPLICATE_NICKNAME);
        }
    }

    private void validateEmailForUpdate(final User user, final String newEmail) {
        if (user.getEmail().equals(newEmail)) {
            return;
        }
        if (userRepository.existsByEmail(newEmail)) {
            throw new BusinessException(DUPLICATE_EMAIL);
        }
    }
}