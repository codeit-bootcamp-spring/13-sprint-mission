package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.config.JwtProperties;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryJwtRegistry implements JwtRegistry {

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final int maxActiveJwtCount;
  private final JwtTokenProvider jwtTokenProvider;

  @Autowired
  public InMemoryJwtRegistry(JwtProperties properties, JwtTokenProvider jwtTokenProvider) {
    this(properties.getMaxActiveJwtCount(), jwtTokenProvider);
  }

  InMemoryJwtRegistry(int maxActiveJwtCount, JwtTokenProvider jwtTokenProvider) {
    if (maxActiveJwtCount < 1) {
      throw new IllegalArgumentException("최대 활성 JWT 개수는 1 이상이어야 합니다.");
    }
    this.maxActiveJwtCount = maxActiveJwtCount;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  @CacheEvict(cacheNames = {"users", "channelsByUser"}, allEntries = true)
  public void registerJwtInformation(JwtInformation jwtInformation) {
    UUID userId = jwtInformation.getUserDto().id();
    origin.compute(userId, (ignored, existing) -> {
      Queue<JwtInformation> informationQueue = existing == null
          ? new ConcurrentLinkedQueue<>()
          : existing;
      while (informationQueue.size() >= maxActiveJwtCount) {
        informationQueue.poll();
      }
      informationQueue.offer(jwtInformation);
      return informationQueue;
    });
  }

  @Override
  @CacheEvict(cacheNames = {"users", "channelsByUser"}, allEntries = true)
  public void invalidateJwtInformationByUserId(UUID userId) {
    if (userId != null) {
      origin.remove(userId);
    }
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> informationQueue = origin.get(userId);
    return informationQueue != null && informationQueue.stream().anyMatch(this::isActive);
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(information -> information.getAccessToken().equals(accessToken)
            && isActive(information));
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(information -> information.getRefreshToken().equals(refreshToken)
            && isActive(information));
  }

  @Override
  @CacheEvict(cacheNames = {"users", "channelsByUser"}, allEntries = true,
      condition = "#result == true")
  public boolean rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
    UUID userId = newJwtInformation.getUserDto().id();
    final boolean[] rotated = {false};
    origin.computeIfPresent(userId, (ignored, informationQueue) -> {
      JwtInformation current = informationQueue.stream()
          .filter(information -> information.getRefreshToken().equals(refreshToken))
          .filter(this::isActive)
          .findFirst()
          .orElse(null);
      if (current == null) {
        return informationQueue;
      }

      informationQueue.remove(current);
      while (informationQueue.size() >= maxActiveJwtCount) {
        informationQueue.poll();
      }
      informationQueue.offer(newJwtInformation);
      rotated[0] = true;
      return informationQueue;
    });
    return rotated[0];
  }

  @Scheduled(fixedDelayString = "${discodeit.security.jwt.cleanup-fixed-delay:PT5M}")
  @Override
  @CacheEvict(cacheNames = {"users", "channelsByUser"}, allEntries = true)
  public void clearExpiredJwtInformation() {
    origin.forEach((userId, informationQueue) -> {
      informationQueue.removeIf(information -> !isActive(information));
      if (informationQueue.isEmpty()) {
        origin.remove(userId, informationQueue);
      }
    });
    log.debug("만료된 JWT 정보 정리 완료: activeUserCount={}", origin.size());
  }

  private boolean isActive(JwtInformation information) {
    return jwtTokenProvider.validateRefreshToken(information.getRefreshToken());
  }
}
