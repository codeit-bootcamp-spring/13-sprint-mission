package com.sprint.mission.discodeit.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.AbstractGroupExpression;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.discodeit.dto.projection.ChannelProjection;
import com.sprint.mission.discodeit.entity.*;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.*;

// todo - 파일 데이터 입출력 클래스를 adaptor 레이어로 바꾸어 쿼리 결과 데이터 측에 추가한다.

@RequiredArgsConstructor
public class ChannelQueryDslImpl implements ChannelQueryDsl {

    private final JPAQueryFactory jpaQueryFactory;

    private final QChannel channel = QChannel.channel;
    private final QUser user = QUser.user;
    private final QReadStatus readStatus = QReadStatus.readStatus;
    private final QMessage message = QMessage.message;

    @Override
    public Optional<ChannelProjection> getChannelById(UUID id){
        Map<UUID, ChannelProjection> result = jpaQueryFactory.selectFrom(channel)
                .leftJoin(readStatus.channel, channel)
                .leftJoin(readStatus.user, user)
                .join(message.channel, channel)
                .where(channel.id.eq(id))
                .transform(
                        GroupBy.groupBy(channel.id).as(
                                Projections.constructor(
                                        ChannelProjection.class,
                                        channel.id,
                                        channel.type,
                                        channel.name,           // nullable (public)
                                        channel.description,    // nullable (public)
                                        userIdList(),
                                        lastMessageAt()
                                )
                        )
                );
        return Optional.ofNullable(result.get(id));
    }


    @Override
    public Collection<ChannelProjection> getChannelsFromUserId(UUID id) {
        Map<UUID, ChannelProjection> result = jpaQueryFactory.selectFrom(channel)
                .leftJoin(readStatus.channel, channel)
                .leftJoin(readStatus.user, user)
                .join(message.channel, channel)
                .where(channelQueryCondition(id))
                .transform(
                        GroupBy.groupBy(channel.id).as(
                                Projections.constructor(
                                        ChannelProjection.class,
                                        channel.id,
                                        channel.type,
                                        channel.name,           // nullable (public)
                                        channel.description,    // nullable (public)
                                        userIdList(),
                                        lastMessageAt()
                                )
                        )
                );

        return result.values();
    }

    // channel query condition.
    // 1. user joined private channel.
    // 2. public channel.
    private BooleanBuilder channelQueryCondition(UUID id){
        BooleanBuilder condition = new BooleanBuilder();
        condition.or(user.id.eq(id));
        condition.or(channel.type.eq(ChannelType.PUBLIC));

        return condition;
    }


    private AbstractGroupExpression<UUID, List<UUID>> userIdList(){
        return  GroupBy.list( user.id );
    }

    private JPQLQuery<Instant> lastMessageAt(){
        return JPAExpressions  // last message
                .select(message.createdAt)
                .from(message)
                .where(message.channel.eq(channel))
                .orderBy(message.createdAt.desc())
                .limit(1L);
    }
}
