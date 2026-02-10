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
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public Long createUser(UserCreateRequest request) {

        validateDuplicateNickname(request.nickname());
        validateDuplicateEmail(request.email());

        User user = User.createUser(request.nickname(), request.email(), request.password());
        User savedUser = userRepository.save(user);

        return savedUser.getId();
    }

    @Transactional
    public UserUpdateResponse updateUser(Long userId, UserUpdateRequest request) {

        // TODO: Implement security check
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        validateForUpdate(user, request);
        user.update(request.nickname(), request.email());

        return UserUpdateResponse.from(user);
    }

    private void validateForUpdate(final User user, final UserUpdateRequest request) {
        user.isSameValueWhenUpdate(request);

        if (user.isDifferentNickname(request.nickname())) {
            validateDuplicateNickname(request.nickname());
        }

        if (user.isDifferentEmail(request.email())) {
            validateDuplicateEmail(request.email());
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if(userRepository.existsByNickname(nickname)) {
            throw new BusinessException(DUPLICATE_NICKNAME);
        }
    }

    private void validateDuplicateEmail(String email) {
        if(userRepository.existsByEmail(email)) {
            throw new BusinessException(DUPLICATE_EMAIL);
        }
    }
}