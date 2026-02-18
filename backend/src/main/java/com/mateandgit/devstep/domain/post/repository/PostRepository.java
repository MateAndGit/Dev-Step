package com.mateandgit.devstep.domain.post.repository;

import com.mateandgit.devstep.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
}
