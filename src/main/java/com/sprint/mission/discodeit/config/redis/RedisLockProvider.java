package com.sprint.mission.discodeit.config.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisLockProvider {

    /*
    동시성 제어를 위해 "애플리케이션"이 락을 식별하기 위한 클래스.
    Redis 는 락을 의미하는 값을 가지고 있어서, 애플리케이션이 해당 락을 확인하는 코드를 짜야 함.

    AI - 리뷰 -> 락 해제 방식에 문자가 있러서 다름 요청의 락을 지울 수 도 있다?
    -> lockValue 가 일반 UUID 생성보다 충돌(중복) 될 가능성이 높다.
    -> 락 해제 부분에 "누가 건 락인지" 가 없다. 때문에 TTL 만료 이후 동시사용에서 다른 유저의 락을 삭제 가능하다.
    + get  -> delete 가 아닌 원자적 삭제가 필요.
    트랜젝션에 걸리는 시간이 10초보다 길면, 다른 요청작업이 시행될 수 있다.
     */

    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(10);    // 락이 자동 만료되기까지의 시간.
    private static final String LOCK_KEY_PREFIX = "lock:";  // 일반 데이터와 구별을 위한 접두어

    private final RedisTemplate<String, Object> redisTemplate;

    public void acquireLock(String key) {
        String lockKey = LOCK_KEY_PREFIX + key;
        String lockValue = Thread.currentThread().getName() + "-" + System.currentTimeMillis();
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();

        // SETNX: 키가 없으면 설정하고 TTL 지정
        Boolean acquired = valueOps.setIfAbsent(lockKey, lockValue, LOCK_TIMEOUT);

        // acquired => true 비교가 아닌 순서를 반대로 함으로서 NPE 방지.
        if (Boolean.TRUE.equals(acquired)) {
            log.debug("분산 락 획득 성공: {} (값: {})", lockKey, lockValue);
        } else {
            log.debug("분산 락 획득 실패: {}", lockKey);
            throw new RedisLockAcquisitionException("분산 락 획득 실패: " + lockKey);
        }
    }

    public void releaseLock(String key) {
        String lockKey = LOCK_KEY_PREFIX + key;
        try {
            redisTemplate.delete(lockKey);
            log.debug("분산 락 해제 완료: {}", lockKey);
        } catch (Exception e) {
            log.warn("분산 락 해제 실패: {}", lockKey, e);
        }
    }
    public static class RedisLockAcquisitionException extends RuntimeException {

        public RedisLockAcquisitionException(String message) {
            super(message);
        }
    }
}

