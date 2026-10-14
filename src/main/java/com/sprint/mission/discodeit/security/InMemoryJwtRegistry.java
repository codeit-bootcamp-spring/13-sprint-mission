package com.sprint.mission.discodeit.security;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InMemoryJwtRegistry implements JwtRegistry {

  private static final int MAX_ACTIVE_JWT_COUNT = 1;

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();

  private final JwtTokenProvider jwtTokenProvider;

  public InMemoryJwtRegistry(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    UUID userId = jwtInformation.userDto().id();

    origin.compute(userId, (id, queue) -> {
      Queue<JwtInformation> jwtInformations = queue != null ? queue : new ConcurrentLinkedQueue<>();

      while (jwtInformations.size() >= MAX_ACTIVE_JWT_COUNT) {
        jwtInformations.poll();
      }

      jwtInformations.offer(jwtInformation);

      return jwtInformations;
    });
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    origin.remove(userId);
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    Queue<JwtInformation> jwtInformations = origin.get(userId);

    return jwtInformations != null
        && jwtInformations.stream()
        .anyMatch(jwtInformation ->
            !jwtTokenProvider.isExpired(jwtInformation.refreshToken())
        );
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(jwtInformation ->
            jwtInformation.accessToken().equals(accessToken)
        );
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return origin.values().stream()
        .flatMap(Queue::stream)
        .anyMatch(jwtInformation ->
            jwtInformation.refreshToken().equals(refreshToken));
  }

  @Override
  public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
    UUID userId = newJwtInformation.userDto().id();

    origin.computeIfPresent(userId, (id, queue) -> {
      JwtInformation currentJwtInformation = queue.stream()
          .filter(jwtInformation -> jwtInformation.refreshToken().equals(refreshToken))
          .findFirst()
          .orElse(null);

      if (currentJwtInformation == null) {
        return queue;
      }

      JwtInformation rotatedJwtInformation =
          currentJwtInformation.rotate(
              newJwtInformation.accessToken(),
              newJwtInformation.refreshToken()
          );

      queue.remove(currentJwtInformation);
      queue.offer(rotatedJwtInformation);

      return queue;
    });
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    origin.forEach((userId, queue) -> {
      queue.removeIf(jwtInformation -> jwtTokenProvider.isExpired(jwtInformation.refreshToken()));

      if (queue.isEmpty()) {
        origin.remove(userId, queue);
      }
    });
  }
}
