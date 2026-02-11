package com.mateandgit.devstep.domain.user.repository;

import com.mateandgit.devstep.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom{
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
}
