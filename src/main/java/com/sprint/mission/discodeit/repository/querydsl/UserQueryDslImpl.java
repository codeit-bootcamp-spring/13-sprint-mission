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
import com.sprint.mission.discodeit.entity.QUserStatus;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class UserQueryDslImpl implements UserQueryDsl {

    private final JPAQueryFactory jpaQueryFactory;

    private final QUser user = QUser.user;
    private final QBinaryContent binaryContent = QBinaryContent.binaryContent;
    private final QUserStatus userStatus = QUserStatus.userStatus;

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
                .join(user.status, userStatus)
                .where(
                        user.id.in(id)
                ).transform(
                        GroupBy.groupBy(user.id).as(
                                userProjectionConstructor()
                        )
                );

        return users.values();
    }


    // check activation time elapse 5 minutes.
    // if over 5, return offline.
    private BooleanExpression getOnline(){
        // half of micro second.
        long HALF_NANOS = ChronoUnit.MICROS.getDuration().toNanos() / 2;
        // round micro sec format time
        Instant now = Instant.now().plusNanos(HALF_NANOS).truncatedTo(ChronoUnit.MICROS);
        return userStatus.lastActiveAt.loe(now.minus(5 * 60, ChronoUnit.SECONDS));
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
                getOnline(),
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
                .join(user.status, userStatus)
                .where(
                        getCondition(
                                exp
                        )
                ).fetchOne();
    }



}
