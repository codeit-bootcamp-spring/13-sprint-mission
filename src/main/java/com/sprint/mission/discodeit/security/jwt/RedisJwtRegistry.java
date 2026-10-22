package com.sprint.mission.discodeit.security.jwt;


import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.sprint.mission.discodeit.config.redis.RedisLockProvider;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.event.user.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@RequiredArgsConstructor
public class RedisJwtRegistry implements JwtRegistry {

    private static final String USER_JWT_KEY_PREFIX = "jwt:user:";
    private static final String ACCESS_TOKEN_INDEX_KEY = "jwt:access_tokens";
    private static final String REFRESH_TOKEN_INDEX_KEY = "jwt:refresh_tokens";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    private final int maxActiveJwtCount;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisLockProvider redisLockProvider;

    @CacheEvict(value = "users", key = "'all'")
    @Retryable(retryFor = RedisLockProvider.RedisLockAcquisitionException.class, maxAttempts = 10,
            backoff = @Backoff(delay = 100, multiplier = 2))
    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {
        String userKey = getUserKey(jwtInformation.getUserDto().id());
        String lockKey = jwtInformation.getUserDto().id().toString();

        // 락 획득
        redisLockProvider.acquireLock(lockKey);
        try {
            // redis 리스트 조작 자료구조의 사이즈
            Long currentSize = redisTemplate.opsForList().size(userKey);

            // 토큰 리스트를 조회 후, 동시 로그인 가능 갯수 이상이면, 인덱스(사용자 지정 redis 리스트) 에서 제거.
            while (currentSize != null && currentSize >= maxActiveJwtCount) {
                Object oldestTokenObj = redisTemplate.opsForList().leftPop(userKey);    // 리스트 왼쪽요소 제거후 반환.
                if (oldestTokenObj instanceof JwtInformation oldestToken) {
                    removeTokenIndex(oldestToken.getAccessToken(), oldestToken.getRefreshToken());
                }
                currentSize = redisTemplate.opsForList().size(userKey);
            }

            // 토큰 리스트에 새 인증 토큰 추가.
            redisTemplate.opsForList().rightPush(userKey, jwtInformation);
            redisTemplate.expire(userKey, DEFAULT_TTL);
            addTokenIndex(jwtInformation.getAccessToken(), jwtInformation.getRefreshToken());

        } finally {
            // 원자적 락 해제.
            redisLockProvider.releaseLock(lockKey);
        }

        // 로그인 상태 변화에 따른 유저 상태 업데이트 이벤트.
        eventPublisher.publishEvent(
                new UserEvent("users.updated",jwtInformation.getUserDto())
        );
    }

    @CacheEvict(value = "users", key = "'all'")
    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        String userKey = getUserKey(userId);

        AtomicReference<UserDto> deletedUser = new AtomicReference<>();

        // 락 없이 자료형 반환, 삭제?
        List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);
        if (tokens != null) {
            tokens.forEach(tokenObj -> {
                if (tokenObj instanceof JwtInformation jwtInfo) {
                    deletedUser.set(jwtInfo.getUserDto());
                    removeTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());
                }
            });
        }

        redisTemplate.delete(userKey);

        // 삭제가 없다면 null 을 반환해 버리는 문제가 있음.
        // repository 를 써서 UserDto 를 반환하던지, event 에서 userService 를 써서 유저 정보를 반환하는 방식으로 해야 할 듯.
        // todo - 이벤트가 UserDto를 조회해서 반환하는 방식으로 변경?
        eventPublisher.publishEvent(new UserEvent("users.updated", deletedUser.get()));
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        String userKey = getUserKey(userId);
        Long size = redisTemplate.opsForList().size(userKey);
        return size != null && size > 0;
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(ACCESS_TOKEN_INDEX_KEY, accessToken)
        );
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(REFRESH_TOKEN_INDEX_KEY, refreshToken)
        );
    }

    @Retryable(retryFor = RedisLockProvider.RedisLockAcquisitionException.class, maxAttempts = 10,
            backoff = @Backoff(delay = 100, multiplier = 2))
    @Override
    public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
        String userKey = getUserKey(newJwtInformation.getUserDto().id());
        String lockKey = newJwtInformation.getUserDto().id().toString();

        redisLockProvider.acquireLock(lockKey);
        try {
            List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);

            if (tokens != null) {
                for (int i = 0; i < tokens.size(); i++) {
                    if (tokens.get(i) instanceof JwtInformation jwtInfo &&
                            jwtInfo.getRefreshToken().equals(refreshToken)) {

                        removeTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());
                        jwtInfo.rotate(newJwtInformation.getAccessToken(),
                                newJwtInformation.getRefreshToken());
                        redisTemplate.opsForList().set(userKey, i, jwtInfo);
                        addTokenIndex(newJwtInformation.getAccessToken(),
                                newJwtInformation.getRefreshToken());
                        redisTemplate.expire(userKey, DEFAULT_TTL);
                        break;
                    }
                }
            }

        } finally {
            redisLockProvider.releaseLock(lockKey);
        }
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {
        Set<String> userKeys = redisTemplate.keys(USER_JWT_KEY_PREFIX + "*");

        for (String userKey : userKeys) {
            List<Object> tokens = redisTemplate.opsForList().range(userKey, 0, -1);

            if (tokens != null) {
                boolean hasValidTokens = false;

                for (int i = tokens.size() - 1; i >= 0; i--) {
                    if (tokens.get(i) instanceof JwtInformation jwtInfo) {
                        boolean isExpired =
                                !jwtTokenProvider.isExpired(jwtInfo.getAccessToken()) ||
                                        !jwtTokenProvider.isExpired(jwtInfo.getRefreshToken());

                        if (isExpired) {
                            redisTemplate.opsForList().set(userKey, i, "EXPIRED");
                            redisTemplate.opsForList().remove(userKey, 1, "EXPIRED");
                            removeTokenIndex(jwtInfo.getAccessToken(), jwtInfo.getRefreshToken());
                        } else {
                            hasValidTokens = true;
                        }
                    }
                }

                if (!hasValidTokens) {
                    redisTemplate.delete(userKey);
                }
            }
        }
    }

    private String getUserKey(UUID userId) {
        return USER_JWT_KEY_PREFIX + userId.toString();
    }

    private void addTokenIndex(String accessToken, String refreshToken) {
        // Set에 토큰 추가 (add: 중복되면 무시됨)
        redisTemplate.opsForSet().add(ACCESS_TOKEN_INDEX_KEY, accessToken);
        redisTemplate.opsForSet().add(REFRESH_TOKEN_INDEX_KEY, refreshToken);

        // 인덱스 키에도 만료 시간 설정 (메모리 누수 방지)
        redisTemplate.expire(ACCESS_TOKEN_INDEX_KEY, DEFAULT_TTL);
        redisTemplate.expire(REFRESH_TOKEN_INDEX_KEY, DEFAULT_TTL);
    }

    private void removeTokenIndex(String accessToken, String refreshToken) {
        // Set에서 토큰 제거
        redisTemplate.opsForSet().remove(ACCESS_TOKEN_INDEX_KEY, accessToken);
        redisTemplate.opsForSet().remove(REFRESH_TOKEN_INDEX_KEY, refreshToken);
    }
}
