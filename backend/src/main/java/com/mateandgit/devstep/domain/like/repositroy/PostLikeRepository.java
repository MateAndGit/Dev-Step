package com.mateandgit.devstep.domain.like.repositroy;

import com.mateandgit.devstep.domain.like.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
}
