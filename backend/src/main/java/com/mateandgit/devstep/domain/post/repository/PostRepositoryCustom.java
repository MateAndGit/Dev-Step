package com.mateandgit.devstep.domain.post.repository;

import com.mateandgit.devstep.domain.post.dto.request.PostSearchCondition;
import com.mateandgit.devstep.domain.post.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<PostResponse> searchGetPost(Pageable pageable, PostSearchCondition condition);
}
