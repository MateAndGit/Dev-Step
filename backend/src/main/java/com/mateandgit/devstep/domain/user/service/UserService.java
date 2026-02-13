package com.mateandgit.devstep.domain.user.service;

import com.mateandgit.devstep.domain.user.dto.request.UserSearchCondition;
import com.mateandgit.devstep.domain.user.dto.request.UserUpdateRequest;
import com.mateandgit.devstep.domain.user.dto.response.UserResponse;
import com.mateandgit.devstep.domain.user.dto.response.UserUpdateResponse;
import com.mateandgit.devstep.domain.user.entity.User;
import com.mateandgit.devstep.domain.user.repository.UserRepository;
import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.global.security.CustomUserDetails;
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

    public Page<UserResponse> getUserList(Pageable pageable, UserSearchCondition condition, CustomUserDetails userDetails) {
        // TODO: 관리자 권한 체크 로직 (예: userDetails.getAuthorities() 확인)
        return userRepository.searchGetUser(pageable, condition);
    }

    public UserResponse getMyInfo(CustomUserDetails userDetails) {
        return UserResponse.from(userDetails.user());
    }

    public UserResponse getUser(Long userId, CustomUserDetails userDetails) {
        User targetUser = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        // 추가적인 노출 제한 로직이 필요하다면 여기서 처리 (예: 비공개 계정 등)
        return UserResponse.from(targetUser);
    }

    @Transactional
    public UserUpdateResponse updateUser(Long userId, UserUpdateRequest request, CustomUserDetails userDetails) {
        User currentUser = userDetails.user();

        if (!currentUser.getId().equals(userId)) {
            throw new BusinessException(UNAUTHORIZED_ACCESS);
        }

        User targetUser = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        validateForUpdate(targetUser, request);
        targetUser.update(request.nickname(), request.email());

        return UserUpdateResponse.from(targetUser);
    }

    @Transactional
    public Long deleteUser(Long userId, CustomUserDetails userDetails) {
        User currentUser = userDetails.user();

        if (!currentUser.getId().equals(userId)) {
            throw new BusinessException(UNAUTHORIZED_ACCESS);
        }

        User targetUser = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        targetUser.markAsDeleted();
        return targetUser.getId();
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
        if(userRepository.existsByNickname(email)) {
            throw new BusinessException(DUPLICATE_EMAIL);
        }
    }
}