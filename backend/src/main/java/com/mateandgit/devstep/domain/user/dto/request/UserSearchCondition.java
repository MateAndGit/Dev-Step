package com.mateandgit.devstep.domain.user.dto.request;

import com.mateandgit.devstep.global.status.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO 검색 조건 검증
@Getter
@NoArgsConstructor
public class UserSearchCondition {
    private String nickname;
    private String email;
    private UserStatus status;
}
