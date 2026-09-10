package com.sprint.mission.discodeit.repository.querydsl;

import com.querydsl.core.group.GroupBy;
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
import java.util.UUID;

@RequiredArgsConstructor
public class UserQueryDslImpl implements UserQueryDsl {

    private final JPAQueryFactory jpaQueryFactory;

    private final QUser user = QUser.user;
    private final QBinaryContent binaryContent = QBinaryContent.binaryContent;
    private final QUserStatus userStatus = QUserStatus.userStatus;

    @Override
    public Collection<UserProjection> getUserInfoFromIds(UUID... id){
        Map<UUID, UserProjection> users = jpaQueryFactory.selectFrom(user)
                .join(user.profile, binaryContent)
                .join(user.status, userStatus)
                .where(
                        user.profile.eq(binaryContent)
                ).transform(
                        GroupBy.groupBy(user.id).as(
                                Projections.constructor(
                                        UserProjection.class,
                                        user.id,
                                        user.username,
                                        user.email,
                                        getOnline(),
                                        binaryContent.id,
                                        binaryContent.fileName,
                                        binaryContent.size,
                                        binaryContent.contentType
                                )
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




}
