package com.mateandgit.devstep.domain.post.repository;

import com.mateandgit.devstep.domain.post.dto.request.PostSearchCondition;
import com.mateandgit.devstep.domain.post.dto.response.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface PostRepositoryCustom {
    Slice<PostResponse> searchGetPost(Pageable pageable, PostSearchCondition condition);
}
