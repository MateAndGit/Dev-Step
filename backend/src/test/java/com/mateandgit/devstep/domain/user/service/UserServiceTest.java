package com.mateandgit.devstep.domain.user.service;

import com.mateandgit.devstep.domain.user.dto.request.UserSearchCondition;
import com.mateandgit.devstep.domain.user.dto.request.UserUpdateRequest;
import com.mateandgit.devstep.domain.user.dto.response.UserResponse;
import com.mateandgit.devstep.domain.user.dto.response.UserUpdateResponse;
import com.mateandgit.devstep.domain.user.entity.User;
import com.mateandgit.devstep.domain.user.repository.UserRepository;
import com.mateandgit.devstep.global.exception.BusinessException;
import com.mateandgit.devstep.global.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.mateandgit.devstep.global.exception.ErrorCode.UNAUTHORIZED_ACCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        testUser = User.createUser("nick", "test@test.com", "pw123");
        setField(testUser, "id", 1L);
        userDetails = new CustomUserDetails(testUser);
    }

    @Test
    @DisplayName("Successfully get user profile by ID")
    void getUser_Success() {
        // given
        given(userRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(testUser));

        // when
        UserResponse response = userService.getUser(1L, userDetails);

        // then
        assertThat(response.nickname()).isEqualTo("nick");
        verify(userRepository, times(1)).findByIdAndDeletedFalse(anyLong());
    }

    @Test
    @DisplayName("Successfully update own nickname and email")
    void updateUser_Success() {
        // given
        UserUpdateRequest request = new UserUpdateRequest("newNick", "new@test.com");
        given(userRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByNickname("newNick")).willReturn(false);

        // when
        UserUpdateResponse response = userService.updateUser(1L, request, userDetails);

        // then
        assertThat(response.nickname()).isEqualTo("newNick");
        assertThat(testUser.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    @DisplayName("Fail to update user when target ID does not match current user")
    void updateUser_Fail_Unauthorized() {
        // given
        UserUpdateRequest request = new UserUpdateRequest("hacker", "hacker@test.com");
        Long targetUserId = 999L;

        // when & then
        assertThatThrownBy(() -> userService.updateUser(targetUserId, request, userDetails))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(UNAUTHORIZED_ACCESS.getMessage());
    }

    @Test
    @DisplayName("Successfully mark user as deleted (Soft Delete)")
    void deleteUser_Success() {
        // given
        given(userRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(testUser));

        // when
        Long deletedId = userService.deleteUser(1L, userDetails);

        // then
        assertThat(deletedId).isEqualTo(1L);
        assertThat(testUser.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Successfully get list of users with pagination")
    void getUserList_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        UserSearchCondition condition = new UserSearchCondition();
        Page<UserResponse> expectedPage = new PageImpl<>(List.of(UserResponse.from(testUser)));

        given(userRepository.searchGetUser(pageable, condition)).willReturn(expectedPage);

        // when
        Page<UserResponse> result = userService.getUserList(pageable, condition, userDetails);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nickname()).isEqualTo("nick");
    }
}