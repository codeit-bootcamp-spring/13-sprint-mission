package com.sprint.mission.discodeit.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class InMemoryJwtRegistry implements JwtRegistry {

    /**
     * <userId, Queue<JwtInformation>>
     */
    private final Map<UUID, Queue<JwtInformation>> origin =
            new ConcurrentHashMap<>();

    /**
     * 사용자 한 명이 동시에 유지할 수 있는
     * 최대 JWT 로그인 정보 개수.
     *
     * 현재 요구사항은 1개이다.
     */
    private final int maxActiveJwtCount;

    private final JwtTokenProvider jwtTokenProvider;

    public InMemoryJwtRegistry(
            @Value("${jwt.max-active-count:1}")
            int maxActiveJwtCount,
            JwtTokenProvider jwtTokenProvider
    ) {

        if (maxActiveJwtCount < 1) {

            throw new IllegalArgumentException(
                    "maxActiveJwtCount는 1 이상이어야 합니다."
            );
        }

        this.maxActiveJwtCount =
                maxActiveJwtCount;

        this.jwtTokenProvider =
                jwtTokenProvider;
    }

    /**
     * 로그인 성공 시 JWT 정보를 등록한다.
     *
     * 최대 동시 로그인 수를 초과하면
     * 가장 오래된 JWT 정보를 제거한다.
     */
    @Override
    public void registerJwtInformation(
            JwtInformation jwtInformation
    ) {

        UUID userId =
                jwtInformation
                        .getUserDto()
                        .getId();

        origin.compute(
                userId,
                (key, queue) -> {

                    Queue<JwtInformation> jwtInformationQueue =
                            queue;

                    if (jwtInformationQueue == null) {

                        jwtInformationQueue =
                                new ConcurrentLinkedQueue<>();
                    }

                    while (
                            jwtInformationQueue.size()
                                    >= maxActiveJwtCount
                    ) {

                        jwtInformationQueue.poll();
                    }

                    jwtInformationQueue.offer(
                            jwtInformation
                    );

                    return jwtInformationQueue;
                }
        );
    }

    /**
     * UserId에 해당하는 모든 JWT 정보를 제거한다.
     */
    @Override
    public void invalidateJwtInformationByUserId(
            UUID userId
    ) {

        if (userId == null) {
            return;
        }

        origin.remove(
                userId
        );
    }

    /**
     * 해당 사용자의 JWT 정보가 Registry에 존재하는지 확인한다.
     */
    @Override
    public boolean hasActiveJwtInformationByUserId(
            UUID userId
    ) {

        if (userId == null) {
            return false;
        }

        Queue<JwtInformation> queue =
                origin.get(
                        userId
                );

        return queue != null
                && !queue.isEmpty();
    }

    /**
     * Access Token이 Registry에 존재하는지 확인한다.
     */
    @Override
    public boolean hasActiveJwtInformationByAccessToken(
            String accessToken
    ) {

        if (
                accessToken == null
                        || accessToken.isBlank()
        ) {

            return false;
        }

        return origin.values()
                .stream()
                .flatMap(
                        Queue::stream
                )
                .anyMatch(
                        jwtInformation ->
                                accessToken.equals(
                                        jwtInformation
                                                .getAccessToken()
                                )
                );
    }

    /**
     * Refresh Token이 Registry에 존재하는지 확인한다.
     */
    @Override
    public boolean hasActiveJwtInformationByRefreshToken(
            String refreshToken
    ) {

        if (
                refreshToken == null
                        || refreshToken.isBlank()
        ) {

            return false;
        }

        return origin.values()
                .stream()
                .flatMap(
                        Queue::stream
                )
                .anyMatch(
                        jwtInformation ->
                                refreshToken.equals(
                                        jwtInformation
                                                .getRefreshToken()
                                )
                );
    }

    /**
     * Refresh Token Rotation.
     *
     * 기존 Refresh Token이 들어있는 정보를 찾아서
     * 새로운 JwtInformation으로 교체한다.
     */
    @Override
    public void rotateJwtInformation(
            String refreshToken,
            JwtInformation newJwtInformation
    ) {

        if (
                refreshToken == null
                        || refreshToken.isBlank()
                        || newJwtInformation == null
        ) {

            return;
        }

        UUID userId =
                newJwtInformation
                        .getUserDto()
                        .getId();

        origin.computeIfPresent(
                userId,
                (key, queue) -> {

                    boolean removed =
                            queue.removeIf(
                                    jwtInformation ->
                                            refreshToken.equals(
                                                    jwtInformation
                                                            .getRefreshToken()
                                            )
                            );

                    if (removed) {

                        queue.offer(
                                newJwtInformation
                        );
                    }

                    return queue.isEmpty()
                            ? null
                            : queue;
                }
        );
    }

    /**
     * 만료된 JWT 정보를 Registry에서 제거한다.
     *
     * 로그인 상태 유지 기준은 Refresh Token이므로
     * Refresh Token이 더 이상 유효하지 않은 정보를 제거한다.
     *
     * 5분마다 실행된다.
     */
    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {

        origin.forEach(
                (userId, queue) -> {

                    queue.removeIf(
                            jwtInformation ->
                                    !jwtTokenProvider
                                            .validateRefreshToken(
                                                    jwtInformation
                                                            .getRefreshToken()
                                            )
                    );

                    if (queue.isEmpty()) {

                        origin.remove(
                                userId,
                                queue
                        );
                    }
                }
        );
    }
}