package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@RequiredArgsConstructor
@Slf4j
public class InMemoryJwtRegistry implements JwtRegistry{

    private final Map<UUID , Queue<JwtInformation>> origin = new ConcurrentHashMap<>();

    private final int maxActiveJwtCount = 1;

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {
        UUID userId = jwtInformation.userDto().id();
        origin.compute(userId, (key, queue) -> {
            if (queue == null) {
                queue = new ConcurrentLinkedQueue<>();
            }

            while (queue.size() >= maxActiveJwtCount) {
                queue.poll();
            }
            queue.add(jwtInformation);
            return queue;
        });
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        origin.remove(userId);
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        Queue<JwtInformation> jwtInformations = origin.get(userId);
        if (jwtInformations == null) return false;

        return true;
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        Optional<JwtInformation> information = origin.values().stream()
                .filter(queue -> queue.stream().anyMatch(jwtInformation -> jwtInformation.accessToken().equals(accessToken)))
                .findFirst()
                .map(Queue::peek);
        if (information.isEmpty()) return false;

        return true;
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        Optional<JwtInformation> information = origin.values().stream()
                .filter(queue -> queue.stream().anyMatch(jwtInformation -> jwtInformation.refreshToken().equals(refreshToken)))
                .findFirst()
                .map(Queue::peek);
        if (information.isEmpty()) return false;
        return true;
    }

    @Override
    public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
        UUID userId = newJwtInformation.userDto().id();
        origin.compute(userId, (key, queue) -> {
            if (queue == null || queue.stream().noneMatch(info -> info.refreshToken().equals(refreshToken))) {
                throw new TokenRenewalFailedException();
            }
            // 검증과 교체를 사용자별로 원자적으로 처리하여 같은 토큰의 중복 갱신을 막는다.
            queue.removeIf(info -> info.refreshToken().equals(refreshToken));
            while (queue.size() >= maxActiveJwtCount) {
                queue.poll();
            }
            queue.add(newJwtInformation);
            return queue;
        });
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {
        // 등록·로테이션과 같은 사용자별 잠금 안에서 검사와 삭제를 완료한다.
        origin.keySet().forEach(userId -> origin.computeIfPresent(userId, (key, queue) -> {
            queue.removeIf(info -> jwtTokenProvider.validateToken(info.refreshToken()).isEmpty());
            return queue.isEmpty() ? null : queue;
        }));
    }

}
