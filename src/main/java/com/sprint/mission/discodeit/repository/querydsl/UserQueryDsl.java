package com.sprint.mission.discodeit.repository.querydsl;

import com.sprint.mission.discodeit.dto.projection.UserProjection;

import java.util.*;

public interface UserQueryDsl {

    /**
     * 유저 id 를 기반으로 유저 정보 쿼리.
     * @param id UUID
     * @return UserProjection
     */
    Optional<UserProjection> getUserFromId(UUID id);

    /**
     * 유저 이름을 기반으로 유저 정보 반환.
     * @param username String
     * @return UserProjection
     */
    Optional<UserProjection> getUserFromUsername(String username);

    /**
     * 아이디에 해당하는 유저 정보(BinaryContent 데이터 포함) 반환.
     * @param id UUID...
     * @return UserProjection
     */
    Map<UUID,UserProjection> getUsersFromIds(List<UUID> id);

    Map<UUID,UserProjection> getAllUsers();
}
