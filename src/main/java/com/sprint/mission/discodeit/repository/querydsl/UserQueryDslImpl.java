package com.sprint.mission.discodeit.repository.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.entity.QBinaryContent;
import com.sprint.mission.discodeit.entity.QUser;
import com.sprint.mission.discodeit.security.role.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class UserQueryDslImpl implements UserQueryDsl {

    private final JPAQueryFactory jpaQueryFactory;

    private final QUser user = QUser.user;
    private final QBinaryContent binaryContent = QBinaryContent.binaryContent;

    @RequiredArgsConstructor
    @Getter
    public static class QueryDto{
        public final UUID id;
        public final String username;
        public final String email;
        public final String password;
        public final Role role;
        public final UUID profileId;
        public final String fileName;
        public final Long size;
        public final String contentType;
    }


    @Override
    public Optional<UserProjection> getUserFromId(UUID id){
        List<QueryDto> result = query(user.id.eq(id));
        return convertProjectionFromDto(result).stream().findFirst();
    }

    @Override
    public Optional<UserProjection> getUserFromUsername(String username){
        List<QueryDto> result = query(user.email.eq(username));
        return convertProjectionFromDto(result).stream().findFirst();
    }

    @Override
    public Collection<UserProjection> getUserInfoFromIds(UUID... id){
        List<QueryDto> result = query(user.id.in(id));
        return convertProjectionFromDto(result);
    }


    private Collection<UserProjection> convertProjectionFromDto(List<QueryDto> list){
        return list.stream().collect(
                Collectors.groupingBy(
                        q -> q.id,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                l -> {
                                    // user 가 중복된 경우 -> 그룹핑 or 조회 에러
                                    if (l.size() > 1) log.debug("UserQueryDsl - id 중복 그룹 에러 : {}",l);
                                    QueryDto target = l.get(0);

                                    return new UserProjection(
                                            target.id,
                                            target.username,
                                            target.email,
                                            target.password,
                                            target.role,
                                            target.profileId,
                                            target.fileName,
                                            target.size,
                                            target.contentType
                                    );
                                }
                        )
                )
        ).values();
    }




    private BooleanBuilder getCondition(BooleanExpression... expressions){
        BooleanBuilder condition = new BooleanBuilder();

        for (BooleanExpression exp : expressions){
            condition.and(exp);
        }
        return condition;
    }

    // user data transfer object constructor for convert user dto.
    private ConstructorExpression<QueryDto> userProjectionConstructor(){
        return Projections.constructor(
                QueryDto.class,
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

    private List<QueryDto> query(BooleanExpression... exps){
        return jpaQueryFactory
                .select(userProjectionConstructor())
                .from(user)
                .leftJoin(user.profile, binaryContent)
                .where(
                        getCondition(exps)
                ).fetch();
    }



}
