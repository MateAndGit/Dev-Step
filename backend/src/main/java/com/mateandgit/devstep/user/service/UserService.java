package com.mateandgit.devstep.user.service;

import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.user.dto.RequestCreateUser;
import com.mateandgit.devstep.user.entity.User;
import com.mateandgit.devstep.user.repository.UserRepository;
import com.mateandgit.devstep.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mateandgit.devstep.global.exception.ErrorCode.DUPLICATE_EMAIL;
import static com.mateandgit.devstep.global.exception.ErrorCode.DUPLICATE_NICKNAME;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public Long createUser(RequestCreateUser request) {

        ValidationUtils.validateNickname(request.nickname());
        ValidationUtils.validateEmail(request.email());
        //TODO Implement validate password

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
}
