package com.mateandgit.devstep.domain.post.repository;

import com.mateandgit.devstep.domain.post.dto.request.PostSearchCondition;
import com.mateandgit.devstep.domain.post.dto.response.PostResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.mateandgit.devstep.domain.post.entity.QPost.post;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<PostResponse> searchGetPost(Pageable pageable, PostSearchCondition condition) {

        List<PostResponse> content = queryFactory
                .select(Projections.constructor(PostResponse.class,
                        post.id,
                        post.title,
                        post.content,
                        post.author.nickname,
                        post.createdAt
                ))
                .from(post)
                .join(post.author)
                .where(
                        postTitleEq(condition.title()),
                        postContent(condition.content()),
                        postAuthorNickname(condition.authorNickname())
                )
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression postTitleEq(String title) {
        return StringUtils.hasText(title) ? post.title.contains(title) : null;
    }

    private BooleanExpression postContent(String content) {
        return StringUtils.hasText(content) ? post.content.contains(content) : null;
    }

    private BooleanExpression postAuthorNickname(String authorNickname) {
        return StringUtils.hasText(authorNickname) ? post.author.nickname.contains(authorNickname) : null;
    }
}