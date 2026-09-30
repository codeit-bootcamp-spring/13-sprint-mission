package com.sprint.mission.discodeit.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.discodeit.dto.projection.MessageProjection;
import com.sprint.mission.discodeit.entity.QBinaryContent;
import com.sprint.mission.discodeit.entity.QMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MessageQueryDslImpl implements MessageQueryDsl{

    private final JPAQueryFactory jpaQueryFactory;

    private final QMessage message = QMessage.message;
    private final QBinaryContent binaryContent = QBinaryContent.binaryContent;

    @RequiredArgsConstructor
    @Getter
    static public class QueryDto{
        public final UUID id;
        public final Instant ctime;
        public final Instant mtime;
        public final String content;
        public final UUID channelId;
        public final UUID userId;
        public final UUID file;
    }


    @Override
    public Map<UUID, MessageProjection> getMessageProjectionFromIdList(List<UUID> list){
        return list.isEmpty()
                ? new HashMap<>()
                : convert(query(message.id.in(list)));
    }

    /*
    변환 관련 매서드
     */

    private Map<UUID, MessageProjection> convert(List<QueryDto> query){
        return query.stream().collect(
                Collectors.groupingBy(
                        q -> q.id,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    QueryDto main = list.get(0);

                                    return new MessageProjection(
                                            main.id,
                                            main.ctime,
                                            main.mtime,
                                            main.content,
                                            main.channelId,
                                            main.userId,
                                            list.stream().map(QueryDto::getFile).toList()
                                    );
                                }
                        )
                )
        );
    }

    /*
    쿼리 관련 매서드
     */

    private List<QueryDto> query(BooleanExpression... exps){
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                            QueryDto.class,
                                message.id,
                                message.createdAt,
                                message.updatedAt,
                                message.content,
                                message.content,
                                message.channel.id,
                                message.author.id,
                                binaryContent.id
                        )
                )
                .from(message)
                .leftJoin(message.attachment, binaryContent)
                .where(condition(exps))
                .orderBy(message.createdAt.desc())
                .fetch();
    }

    private BooleanBuilder condition(BooleanExpression... exps){
        BooleanBuilder where = new BooleanBuilder();
        for (BooleanExpression exp : exps){
            where.and(exp);
        }
        return where;
    }

}
