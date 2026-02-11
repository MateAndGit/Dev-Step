package com.mateandgit.devstep.domain.user.dto.response;

import com.mateandgit.devstep.domain.user.entity.User;
import lombok.Builder;

@Builder
public record UserUpdateResponse(
        Long id,
        String nickname,
        String email
) {
    public static UserUpdateResponse from(User user) {
        return UserUpdateResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .build();
    }
}
