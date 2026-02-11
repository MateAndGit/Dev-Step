package com.mateandgit.devstep.domain.user.repository;

import com.mateandgit.devstep.domain.user.dto.request.UserSearchCondition;
import com.mateandgit.devstep.domain.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<UserResponse> searchGetUser(Pageable pageable, UserSearchCondition condition);
}
