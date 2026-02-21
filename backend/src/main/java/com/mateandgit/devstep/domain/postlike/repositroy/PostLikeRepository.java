package com.mateandgit.devstep.domain.postlike.repositroy;

import com.mateandgit.devstep.domain.postlike.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
}
