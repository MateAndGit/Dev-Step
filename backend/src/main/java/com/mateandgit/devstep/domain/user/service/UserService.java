package com.mateandgit.devstep.domain.user.service;

import com.mateandgit.devstep.domain.user.dto.request.UserCreateRequest;
import com.mateandgit.devstep.domain.user.dto.request.UserSearchCondition;
import com.mateandgit.devstep.domain.user.dto.request.UserUpdateRequest;
import com.mateandgit.devstep.domain.user.dto.response.UserResponse;
import com.mateandgit.devstep.domain.user.dto.response.UserUpdateResponse;
import com.mateandgit.devstep.domain.user.entity.User;
import com.mateandgit.devstep.domain.user.repository.UserRepository;
import com.mateandgit.devstep.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<UserResponse> getUserList(Long adminId, Pageable pageable,  UserSearchCondition condition) {

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        // TODO Implement checkAuthority

        return userRepository.searchGetUser(pageable, condition);
    }

    public UserResponse getUser(Long userId) {

        // TODO: Implement security check
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        // TODO getUser 시 삭제된 유저인지 체크
        if (user.isDeleted()) {
            throw new BusinessException(USER_NOT_FOUND);
        }

        // TODO Implement checkAuthority

        return UserResponse.from(user);
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

    @Transactional
    public Long deleteUser(Long userId) {

        // TODO: Implement security check
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        user.markAsDeleted();

        return user.getId();
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