package com.sprint.mission.discodeit.repository;

import java.util.Set;
import java.util.UUID;

/**
 * 토큰 만료에 따른 온라인 상태 변경을 확인할 사용자 ID 집합을 관리한다.
 * 집합에 있다는 사실만으로 현재 온라인임을 뜻하지 않으며 최종 판단은 JWT 상태로 한다.
 */
public interface UserOnlineTrackingRepository {
    void add(UUID userId);

    boolean remove(UUID userId);

    Set<UUID> findAll();
}
