package com.sprint.mission.discodeit.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.discodeit.dto.projection.ChannelProjection;
import com.sprint.mission.discodeit.entity.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

// todo - 파일 데이터 입출력 클래스를 adaptor 레이어로 바꾸어 쿼리 결과 데이터 측에 추가한다.

@RequiredArgsConstructor
@Repository
@Slf4j
public class ChannelQueryDslImpl implements ChannelQueryDsl {

    private final JPAQueryFactory jpaQueryFactory;

    private final QChannel channel = QChannel.channel;
    private final QUser user = QUser.user;
    private final QReadStatus readStatus = QReadStatus.readStatus;
    private final QMessage message = QMessage.message;

    @RequiredArgsConstructor
    @Getter
    public static class QueryDto{
        public final UUID id;
        public final ChannelType type;
        public final String name;
        public final String description;
        public final UUID userId;
        public final Instant lastMessageAt;
    }




    @Override
    public Optional<ChannelProjection> getChannelById(UUID id){
        if (id == null) return Optional.empty();

        List<QueryDto> result = jpaQuery(
                channelQueryCondition(
                    channel.id.eq(id)
                )
        );

        return convertProjectionFromDto(result).values().stream().findFirst();
    }


    @Override
    public Map<UUID,ChannelProjection> getChannelsFromUserId(UUID id) {
        if (id == null) return new HashMap<>();

        List<QueryDto> result = jpaQuery(
                channelQueryCondition(
                        readStatus.user.id.eq(id),
                        channel.type.eq(ChannelType.PUBLIC)
                )
        );

        return convertProjectionFromDto(result);
    }


    private List<QueryDto> jpaQuery(BooleanBuilder condition){
        return jpaQueryFactory
                .select(dtoConstructorExpression())
                .from(channel)
                .leftJoin(readStatus).on(readStatus.channel.eq(channel))
                .leftJoin(message).on(message.channel.eq(channel))
                .leftJoin(readStatus.user, user)
                .where(condition)
                .fetch();
    }

    private ConstructorExpression<QueryDto> dtoConstructorExpression(){
        return Projections.constructor(
                        QueryDto.class,
                        channel.id,
                        channel.type,
                        channel.name,
                        channel.description,
                        user.id,
                        lastMessageAt()
        );
    }


    /*
    QueryDsl 라이브러리가 transform() 매서드 내부 버그가 있음.
    때문에 쿼리 후, 직접 데이터를 조립하는 로직으로 변환.
     */
    private Map<UUID,ChannelProjection> convertProjectionFromDto(List<QueryDto> dtoList){
        return dtoList.stream()
                .collect(
                        Collectors.groupingBy(
                                q -> q.id,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        // 같은 id 를 가지는 QueryDto = list
                                        list -> {
                                            QueryDto sample = list.get(0);
                                            List<UUID> userIds = list.stream()
                                                    .map(QueryDto::getUserId)
                                                    .collect(Collectors.toList());

                                            return new ChannelProjection(
                                                    sample.id,
                                                    sample.type,
                                                    sample.name,
                                                    sample.description,
                                                    userIds,
                                                    sample.lastMessageAt
                                            );
                                        }
                                )
                        )
                );
    }

    // channel query condition.
    // 1. user joined private channel.
    // 2. public channel.
    private BooleanBuilder channelQueryCondition(BooleanExpression... exps){
        BooleanBuilder condition = new BooleanBuilder();

        for (BooleanExpression exp : exps){
            condition.or(exp);
        }

        return condition;
    }


    /*
    Group by 의 쿼리는 쿼리결과를 WAS 메모리 내부네서 정리하기때문에,
    서브쿼리 객체는 SQL 표현식이 아닌 객체 필드경로로 인식함.
     */
    private JPQLQuery<Instant> lastMessageAt(){
        return JPAExpressions
                .select(message.createdAt.max())
                .from(message)
                .where(message.channel.eq(channel));
//                .orderBy(message.createdAt.desc())
//                .limit(1L);
    }
}
