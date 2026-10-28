package com.sprint.mission.discodeit.redis;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLockProvider {
  private final StringRedisTemplate redis;
  private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>(
      "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end", Long.class);

  public String acquireLock(String key) {
    String owner = UUID.randomUUID().toString();
    if (!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent("lock:" + key, owner, Duration.ofSeconds(10)))) {
      throw new RedisLockAcquisitionException();
    }
    return owner;
  }

  public void releaseLock(String key, String owner) {
    redis.execute(RELEASE, List.of("lock:" + key), owner);
  }

  public static class RedisLockAcquisitionException extends RuntimeException { }
}
