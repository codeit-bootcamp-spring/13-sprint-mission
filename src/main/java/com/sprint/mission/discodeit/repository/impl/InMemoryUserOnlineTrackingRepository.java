package com.sprint.mission.discodeit.repository.impl;

import com.sprint.mission.discodeit.repository.UserOnlineTrackingRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 단일 인스턴스에서 온라인 상태를 확인할 사용자 ID를 동시성 집합으로 관리한다.
 * 조회는 수정 불가능한 복사본을 반환하며 다른 서버와 추적 목록을 공유하지 않는다.
 */
@Repository
@ConditionalOnProperty(prefix = "discodeit.security.jwt", name = "registry-type", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryUserOnlineTrackingRepository implements UserOnlineTrackingRepository {

    private final Set<UUID> onlineUserIds = ConcurrentHashMap.newKeySet();

    @Override
    public void add(UUID userId) {
        onlineUserIds.add(userId);
    }

    @Override
    public boolean remove(UUID userId) {
        return onlineUserIds.remove(userId);
    }

    @Override
    public Set<UUID> findAll() {
        return Set.copyOf(onlineUserIds);
    }
}
