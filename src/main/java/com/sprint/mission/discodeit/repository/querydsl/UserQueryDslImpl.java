package com.sprint.mission.discodeit.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.entity.QBinaryContent;
import com.sprint.mission.discodeit.entity.QUser;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class UserQueryDslImpl implements UserQueryDsl {

    private final JPAQueryFactory jpaQueryFactory;

    private final QUser user = QUser.user;
    private final QBinaryContent binaryContent = QBinaryContent.binaryContent;

    public Optional<UserProjection> getUserFromId(UUID id){
        UserProjection result = singleQuery(user.id.eq(id));
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<UserProjection> getUserFromUsername(String username){
        UserProjection result = singleQuery(user.username.eq(username));
        return Optional.ofNullable(result);
    }

    @Override
    public Collection<UserProjection> getUserInfoFromIds(UUID... id){
        Map<UUID, UserProjection> users = jpaQueryFactory.selectFrom(user)
                .join(user.profile, binaryContent)
                .where(
                        user.id.in(id)
                ).transform(
                        GroupBy.groupBy(user.id).as(
                                userProjectionConstructor()
                        )
                );

        return users.values();
    }


    private BooleanBuilder getCondition(BooleanExpression... expressions){
        BooleanBuilder condition = new BooleanBuilder();

        for (BooleanExpression exp : expressions){
            condition.and(exp);
        }
        return condition;
    }

    // user data transfer object constructor for convert user dto.
    private ConstructorExpression<UserProjection> userProjectionConstructor(){
        return Projections.constructor(
                UserProjection.class,
                user.id,
                user.username,
                user.email,
                user.password,
                user.role,
                binaryContent.id,
                binaryContent.fileName,
                binaryContent.size,
                binaryContent.contentType
        );
    }

    private UserProjection singleQuery(BooleanExpression exp){
        return jpaQueryFactory.select(userProjectionConstructor())
                .from(user)
                .join(user.profile, binaryContent)
                .where(
                        getCondition(
                                exp
                        )
                ).fetchOne();
    }



}
