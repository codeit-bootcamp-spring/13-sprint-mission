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
public class InMemoryJwtRegistry implements JwtRegistry {

    private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
    private final int maxActiveJwtCount = 1;

    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {
        UUID userId = jwtInformation.getUserDto().id();

        // userId에 해당하는 큐가 없으면 새 큐를 생성해 origin에 등록
        Queue<JwtInformation> queue = origin.computeIfAbsent(
                userId,
                key -> new ConcurrentLinkedQueue<>()
        );

        // 새로운 JWT 정보 등록
        queue.offer(jwtInformation);

        // 최대 동시 로그인 수를 초과하면 가장 오래된 JWT 정보 제거
        while (queue.size() > maxActiveJwtCount) {
            queue.poll();
        }
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        // 해당 사용자의 모든 JWT 정보 삭제
        origin.remove(userId);
    }

    @Override
    public void invalidateJwtInformationByRefreshToken(String refreshToken) {
        origin.forEach((userId, queue) -> {
            // 전달받은 Refresh Token과 일치하는 JwtInformation 제거
            queue.removeIf(jwtInformation ->
                    jwtInformation.getRefreshToken().equals(refreshToken));

            // JWT 정보가 남아있지 않으면 해당 사용자 행도 제거
            if (queue.isEmpty()) {
                origin.remove(userId, queue);
            }
        });
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        // 해당 사용자의 활성 JWT 정보가 존재하는지 확인
        Queue<JwtInformation> queue = origin.get(userId);

        return queue != null && !queue.isEmpty();
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        // 등록된 JWT 정보 중 일치하는 Access Token이 있는지 확인
        return origin.values().stream()
                .flatMap(Queue::stream)
                .anyMatch(jwtInformation ->
                        jwtInformation.getAccessToken().equals(accessToken));
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        // 등록된 JWT 정보 중 일치하는 Refresh Token이 있는지 확인
        return origin.values().stream()
                .flatMap(Queue::stream)
                .anyMatch(jwtInformation ->
                        jwtInformation.getRefreshToken().equals(refreshToken));
    }

    @Override
    public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
        // 기존 Refresh Token에 해당하는 JWT 정보 제거
        origin.values().forEach(queue ->
                queue.removeIf(jwtInformation ->
                        jwtInformation.getRefreshToken().equals(refreshToken))
        );

        // 새 JWT 정보 등록
        registerJwtInformation(newJwtInformation);
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {
        origin.forEach((userId, queue) -> {
            // Refresh Token이 만료되거나 유효하지 않은 JWT 정보 제거
            queue.removeIf(jwtInformation ->
                    !jwtTokenProvider.isValid(jwtInformation.getRefreshToken()));

            // JWT 정보가 남아있지 않으면 해당 사용자 행도 제거
            if (queue.isEmpty()) {
                origin.remove(userId, queue);
            }
        });
    }
}
