package com.mateandgit.devstep.domain.user.repository;

import com.mateandgit.devstep.domain.user.dto.request.UserSearchCondition;
import com.mateandgit.devstep.domain.user.dto.response.UserResponse;
import com.mateandgit.devstep.global.status.UserStatus;
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

import static com.mateandgit.devstep.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<UserResponse> searchGetUser(Pageable pageable, UserSearchCondition condition) {

        // 1. 컨텐츠 조회
        List<UserResponse> content = queryFactory
                .select(Projections.constructor(UserResponse.class,
                        user.id,
                        user.nickname,
                        user.email,
                        user.createdAt
                ))
                .from(user)
                .where(
                        nicknameEq(condition.getNickname()),
                        statusEq(condition.getStatus())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        nicknameEq(condition.getNickname()),
                        statusEq(condition.getStatus())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nicknameEq(String nickname) {
        return StringUtils.hasText(nickname) ? user.nickname.contains(nickname) : null;
    }

    private BooleanExpression statusEq(UserStatus status) {
        return status != null ? user.status.eq(status) : null;
    }
}