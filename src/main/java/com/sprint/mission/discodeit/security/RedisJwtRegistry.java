package com.sprint.mission.discodeit.security;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.JwtProperties;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.redis.RedisLockProvider;
import com.sprint.mission.discodeit.realtime.RealtimePublisher;
import java.util.*;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "discodeit.security.jwt.registry", havingValue = "redis", matchIfMissing = true)
public class RedisJwtRegistry implements JwtRegistry {
  private static final String USERS = "jwt:users";
  private final StringRedisTemplate redis;
  private final ObjectMapper mapper;
  private final JwtProperties properties;
  private final JwtTokenProvider tokens;
  private final RedisLockProvider locks;
  private final RealtimePublisher realtime;

  public record StoredJwt(UserResponse user, String access, String refresh) { }

  @Override
  @Retryable(retryFor = RedisLockProvider.RedisLockAcquisitionException.class,
      maxAttempts = 5, backoff = @Backoff(delay = 100, multiplier = 2))
  @CacheEvict(cacheNames = {"users", "channelsByUser"}, allEntries = true)
  public void registerJwtInformation(JwtInformation information) {
    if (properties.getMaxActiveJwtCount() < 1) {
      throw new IllegalArgumentException("최대 활성 JWT 개수는 1 이상이어야 합니다.");
    }
    UUID id = information.getUserDto().id();
    String owner = locks.acquireLock(id.toString());
    try {
      List<StoredJwt> list = load(id);
      list.removeIf(jwt -> !tokens.validateRefreshToken(jwt.refresh()));
      while (list.size() >= properties.getMaxActiveJwtCount()) {
        list.remove(0);
      }
      list.add(new StoredJwt(information.getUserDto(), information.getAccessToken(), information.getRefreshToken()));
      store(id, list, owner);
    } finally {
      locks.releaseLock(id.toString(), owner);
    }
    online(information.getUserDto(), true);
  }

  @Override
  @Retryable(retryFor = RedisLockProvider.RedisLockAcquisitionException.class,
      maxAttempts = 5, backoff = @Backoff(delay = 100, multiplier = 2))
  @CacheEvict(cacheNames = {"users", "channelsByUser"}, allEntries = true)
  public void invalidateJwtInformationByUserId(UUID id) {
    String owner = locks.acquireLock(id.toString());
    try {
      List<StoredJwt> list = load(id);
      delete(id, owner);
      if (!list.isEmpty()) {
        online(list.get(0).user(), false);
      }
    } finally {
      locks.releaseLock(id.toString(), owner);
    }
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID id) {
    return load(id).stream().anyMatch(jwt -> tokens.validateRefreshToken(jwt.refresh()));
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String token) {
    if (!tokens.validateAccessToken(token)) { return false; }
    return load(tokens.getUserId(token)).stream()
        .anyMatch(jwt -> jwt.access().equals(token) && tokens.validateRefreshToken(jwt.refresh()));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String token) {
    if (!tokens.validateRefreshToken(token)) { return false; }
    return load(tokens.getUserId(token)).stream().anyMatch(jwt -> jwt.refresh().equals(token));
  }

  @Override
  @Retryable(retryFor = RedisLockProvider.RedisLockAcquisitionException.class,
      maxAttempts = 5, backoff = @Backoff(delay = 100, multiplier = 2))
  @CacheEvict(cacheNames = {"users", "channelsByUser"}, allEntries = true, condition = "#result == true")
  public boolean rotateJwtInformation(String refreshToken, JwtInformation replacement) {
    UUID id = replacement.getUserDto().id();
    String owner = locks.acquireLock(id.toString());
    try {
      List<StoredJwt> list = load(id);
      boolean removed = list.removeIf(jwt -> jwt.refresh().equals(refreshToken)
          && tokens.validateRefreshToken(jwt.refresh()));
      if (!removed) { return false; }
      list.add(new StoredJwt(replacement.getUserDto(), replacement.getAccessToken(), replacement.getRefreshToken()));
      store(id, list, owner);
      return true;
    } finally {
      locks.releaseLock(id.toString(), owner);
    }
  }

  @Override
  @Scheduled(fixedDelayString = "${discodeit.security.jwt.cleanup-fixed-delay:PT5M}")
  @CacheEvict(cacheNames = {"users", "channelsByUser"}, allEntries = true)
  public void clearExpiredJwtInformation() {
    Set<String> ids = redis.opsForSet().members(USERS);
    if (ids == null) { return; }
    for (String value : ids) {
      UUID id = UUID.fromString(value);
      String owner;
      try { owner = locks.acquireLock(value); }
      catch (RedisLockProvider.RedisLockAcquisitionException busy) { continue; }
      try {
        List<StoredJwt> list = load(id);
        UserResponse user = list.isEmpty() ? null : list.get(0).user();
        list.removeIf(jwt -> !tokens.validateRefreshToken(jwt.refresh()));
        if (list.isEmpty()) {
          delete(id, owner);
          if (user != null) { online(user, false); }
        } else { store(id, list, owner); }
      } finally { locks.releaseLock(value, owner); }
    }
  }

  private List<StoredJwt> load(UUID id) {
    String json = redis.opsForValue().get(key(id));
    if (json == null) { return new ArrayList<>(); }
    try { return mapper.readValue(json, new TypeReference<ArrayList<StoredJwt>>() { }); }
    catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new IllegalStateException("Redis JWT 역직렬화 실패", e);
    }
  }

  private static final DefaultRedisScript<Long> STORE =
      new DefaultRedisScript<>(
          "if redis.call('get',KEYS[1]) ~= ARGV[1] then return 0 end "
              + "redis.call('set',KEYS[2],ARGV[2],'PX',ARGV[3]); "
              + "redis.call('sadd',KEYS[3],ARGV[4]); return 1", Long.class);
  private static final DefaultRedisScript<Long> DELETE =
      new DefaultRedisScript<>(
          "if redis.call('get',KEYS[1]) ~= ARGV[1] then return 0 end "
              + "redis.call('del',KEYS[2]); redis.call('srem',KEYS[3],ARGV[2]); return 1", Long.class);

  private void delete(UUID id, String owner) {
    requireLock(redis.execute(DELETE, List.of("lock:" + id, key(id), USERS), owner, id.toString()));
  }

  private void requireLock(Long result) {
    if (!Long.valueOf(1).equals(result)) {
      throw new RedisLockProvider.RedisLockAcquisitionException();
    }
  }

  private void store(UUID id, List<StoredJwt> list, String owner) {
    try {
      // refresh 만료 후에도 정리 작업이 offline 이벤트를 만들 수 있도록 여유를 둔다.
      requireLock(redis.execute(STORE, List.of("lock:" + id, key(id), USERS), owner,
          mapper.writeValueAsString(list),
          Long.toString(properties.getRefreshTokenExpiration().plus(Duration.ofDays(1)).toMillis()),
          id.toString()));
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new IllegalStateException("Redis JWT 직렬화 실패", e);
    }
  }

  private String key(UUID id) { return "jwt:user:" + id; }

  private void online(UserResponse user, boolean online) {
    realtime.sse(null, "users.updated", new UserResponse(user.id(), user.username(),
        user.email(), user.profile(), online, user.role()));
  }
}
