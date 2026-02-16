package com.mateandgit.devstep.domain.post.repository;

import com.mateandgit.devstep.domain.post.dto.request.PostSearchCondition;
import com.mateandgit.devstep.domain.post.dto.response.PostResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.mateandgit.devstep.domain.post.entity.QPost.post;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostResponse> searchGetPost(Pageable pageable, PostSearchCondition condition) {

        List<PostResponse> postResponses = queryFactory
                .select(Projections.constructor(PostResponse.class,
                        post.id,
                        post.title,
                        post.content,
                        post.author.nickname,
                        post.createdAt
                ))
                .from(post)
                .where(
                        postTitleEq(condition.title()),
                        postContent(condition.content()),
                        postAuthorNickname(condition.authorNickname())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        postTitleEq(condition.title()),
                        postContent(condition.content()),
                        postAuthorNickname(condition.authorNickname())
                );

        return PageableExecutionUtils.getPage(postResponses, pageable, countQuery::fetchOne);
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
