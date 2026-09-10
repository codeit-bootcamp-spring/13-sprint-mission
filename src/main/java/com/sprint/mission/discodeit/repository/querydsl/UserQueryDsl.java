package com.sprint.mission.discodeit.repository.querydsl;

import com.sprint.mission.discodeit.dto.projection.UserProjection;

import java.util.Collection;
import java.util.UUID;

public interface UserQueryDsl {
    /**
     * 아이디에 해당하는 유저 정보(BinaryContent 데이터 포함) 반환.
     * @param id UUID...
     * @return UserProjection
     */
    Collection<UserProjection> getUserInfoFromIds(UUID... id);
}
