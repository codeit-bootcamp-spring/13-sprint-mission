package com.sprint.mission.discodeit.repository.impl;

import com.sprint.mission.discodeit.redis.UserOnlineRedisKeys;
import com.sprint.mission.discodeit.repository.UserOnlineTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Redis Set을 통해 모든 인스턴스가 같은 온라인 상태 추적 목록을 조회한다.
 * JWT 변경 Lua에서도 동일한 키를 사용하여 토큰과 추적 목록을 함께 갱신한다.
 */
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "discodeit.security.jwt", name = "registry-type", havingValue = "redis")
public class RedisUserOnlineTrackingRepository implements UserOnlineTrackingRepository {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void add(UUID userId) {
        redisTemplate.opsForSet().add(UserOnlineRedisKeys.TRACKED_USERS_KEY, userId.toString());
    }

    @Override
    public boolean remove(UUID userId) {
        Long remove = redisTemplate.opsForSet().remove(UserOnlineRedisKeys.TRACKED_USERS_KEY, userId.toString());

        if (remove == null || (remove != 0L && remove != 1L)) {
            throw new IllegalStateException("온라인 사용자 추적 제거 결과가 올바르지 않습니다.");
        }

        // 실제 제거 여부를 반환한다. 단순 제거만으로 최신 JWT가 오프라인인지 판단하지는 않는다.
        return remove == 1L;
    }

    @Override
    public Set<UUID> findAll() {
        Set<String> members = redisTemplate.opsForSet().members(UserOnlineRedisKeys.TRACKED_USERS_KEY);
        if (members == null || members.isEmpty()) {
            return Set.of();
        }

        return members.stream()
                .map(UUID::fromString)
                .collect(Collectors.toUnmodifiableSet());
    }
}
