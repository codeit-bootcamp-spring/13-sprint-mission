package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.config.JwtProperties;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "discodeit.jwt",
    name = "registry",
    havingValue = "redis"
)
public class RedisJwtRegistry implements JwtRegistry {

  private static final String KEY_PREFIX = "discodeit:jwt:";
  private static final String ACCESS = "access";
  private static final String REFRESH = "refresh";

  private static final DefaultRedisScript<Long> ROTATE_SCRIPT;

  static {
    ROTATE_SCRIPT = new DefaultRedisScript<>();
    ROTATE_SCRIPT.setResultType(Long.class);
    ROTATE_SCRIPT.setScriptText("""
        if redis.call('HGET', KEYS[1], 'refresh') ~= ARGV[1] then
          return 0
        end
        
        redis.call('HSET', KEYS[1],
          'access', ARGV[2],
          'refresh', ARGV[3])
        
        redis.call('EXPIRE', KEYS[1], tonumber(ARGV[4]))
        return 1
        """);
  }

  private final StringRedisTemplate redisTemplate;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtProperties jwtProperties;

  private String key(UUID userId) {
    return KEY_PREFIX + userId;
  }

  private Duration ttl() {
    return jwtProperties.refreshTokenExpiration();
  }

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    String redisKey = key(jwtInformation.userDto().id());

    redisTemplate.opsForHash().putAll(
        redisKey,
        Map.of(
            ACCESS, jwtInformation.accessToken(),
            REFRESH, jwtInformation.refreshToken()
        )
    );

    redisTemplate.expire(redisKey, ttl());
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    redisTemplate.delete(key(userId));
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Object refreshToken = redisTemplate.opsForHash()
        .get(key(userId), REFRESH);

    return refreshToken instanceof String token
        && !jwtTokenProvider.isExpired(token);
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    try {
      UUID userId = jwtTokenProvider.getUserId(accessToken);

      Object savedToken = redisTemplate.opsForHash()
          .get(key(userId), ACCESS);

      return accessToken.equals(savedToken);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    try {
      UUID userId = jwtTokenProvider.getUserId(refreshToken);

      Object savedToken = redisTemplate.opsForHash()
          .get(key(userId), REFRESH);

      return refreshToken.equals(savedToken);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public void rotateJwtInformation(
      String refreshToken,
      JwtInformation newJwtInformation
  ) {
    String redisKey = key(newJwtInformation.userDto().id());

    redisTemplate.execute(
        ROTATE_SCRIPT,
        List.of(redisKey),
        refreshToken,
        newJwtInformation.accessToken(),
        newJwtInformation.refreshToken(),
        String.valueOf(Math.max(1, ttl().toSeconds()))
    );
  }

  @Override
  public void clearExpiredJwtInformation() {
  }
}
