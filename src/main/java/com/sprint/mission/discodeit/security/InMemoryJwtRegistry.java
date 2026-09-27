package com.sprint.mission.discodeit.security;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@RequiredArgsConstructor
public class InMemoryJwtRegistry implements JwtRegistry{

    private static final int MAX_ACTIVE_JWT_COUNT = 1;
    private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {

        UUID userId = jwtInformation.getUserDto().id();
        origin.compute(userId, (id, existingQueue) -> {
            Queue<JwtInformation> queue = existingQueue != null ? existingQueue : new ConcurrentLinkedQueue<>();
            while (queue.size() >= MAX_ACTIVE_JWT_COUNT) {
                queue.poll();
            }
            queue.offer(jwtInformation);
            return queue;
        });
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        origin.remove(userId);
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        Queue<JwtInformation> informationQueue = origin.get(userId);

        return informationQueue != null && !informationQueue.isEmpty();
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }

        return origin.values().stream()
                .flatMap(Queue::stream)
                .anyMatch(information ->
                        accessToken.equals(information.getAccessToken())
                );
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }
        return origin.values().stream()
                .flatMap(Queue::stream)
                .anyMatch(information -> refreshToken.equals(information.getRefreshToken()));
    }

    @Override
    public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {

        UUID userId = newJwtInformation.getUserDto().id();
        origin.computeIfPresent(userId, (id, queue) -> {
            boolean existingToken = queue.stream().anyMatch(information ->
                    refreshToken.equals(information.getRefreshToken()));

            if(!existingToken) {
                return queue;
            }
            queue.removeIf(information -> refreshToken.equals(information.getRefreshToken()));
            queue.offer(newJwtInformation);

            while (queue.size() > MAX_ACTIVE_JWT_COUNT) {
                queue.poll();
            }
            return queue;
        });
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {
        origin.forEach((userId, jwtInformationQueue) ->
                origin.computeIfPresent(userId, (id, currentQueue) -> {
                    currentQueue.removeIf(jwtInformation ->
                            !jwtTokenProvider.isValid(
                                    jwtInformation.getRefreshToken()
                            )
                    );

                    return currentQueue.isEmpty()
                            ? null
                            : currentQueue;
                })
        );
    }
}
