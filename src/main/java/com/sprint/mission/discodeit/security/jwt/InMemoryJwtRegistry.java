package com.sprint.mission.discodeit.security.jwt;


import com.sprint.mission.discodeit.dto.response.UserDto;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class InMemoryJwtRegistry implements JwtRegistry{

    // 나중에 concurrent linked queue 로 변경?
    private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
    // 최대 동시 사용자 1명.
    private final int maxActiveJwtCount;

    private final JwtTokenProvider accessTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void registerJwtInformation(JwtInformation info) {
        UUID userId = info.getUserDto().id();

        Queue<JwtInformation> queue = getQueueFromUserId(userId);

        // 맨 뒤에 값 넣기
        if (queue.size() >= maxActiveJwtCount) queue.add(info);
        else {
            // 맨 앞(오래된 것) 삭제.
            queue.poll();
            queue.add(info);
        }
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        Queue<JwtInformation> queue = getQueueFromUserId(userId);

        queue.clear();
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        Queue<JwtInformation> queue = getQueueFromUserId(userId);
        return !queue.isEmpty();
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String token) {
        return origin.values().stream().map(
                q -> q.stream()
                        .map(info -> info.getAccessToken().equals(token))
                        .filter(b -> b)
                        .toList()
        ).filter(
                b -> !b.isEmpty()
        ).toList().isEmpty();
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String token) {
        return origin.values().stream().map(
                q -> q.stream()
                        .map(info -> info.getRefreshToken().equals(token))
                        .filter(b -> b)
                        .toList()
        ).filter(
                b -> !b.isEmpty()
        ).toList().isEmpty();
    }

    @Override
    public void rotateJwtInformation(String refresh, JwtInformation accessInfo) {
        /*
        refresh 체크, 리프레쉬가 동일하다면 엑세스 토큰 변경.
         */
        UserDto userDto = accessInfo.getUserDto();

        RefreshTokenService.Output result = refreshTokenService.rotate(refresh);

        switch (result.result()) {
            case REVOKED -> new RuntimeException();
            case EXPIRED -> new RuntimeException();
            case ROTATED -> new RuntimeException();
            case NOT_FOUND -> new RuntimeException();
            case SUCCESS -> {}
        }


        String accessToken = accessTokenProvider.createAccessToken(userDto.username(),userDto.role());
        String refreshToken = refreshTokenService.grant(userDto.id());

        accessInfo.rotate(accessToken,refreshToken);
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {
        origin.values().forEach(this::deleteInExpired);
    }

    private Queue<JwtInformation> getQueueFromUserId(UUID userId){
        return origin.computeIfAbsent(userId,key -> new ConcurrentLinkedQueue<>());
    }

    private void deleteInExpired(Queue<JwtInformation> q){
        if (q.size() > 1) {
            q.forEach(
                    info -> {
                        String accessToken = info.getAccessToken();
                        Claims claims = accessTokenProvider.parseClaims(accessToken);
                        if (accessTokenProvider.isExpired(claims)) q.remove(info);
                    }
            );
        }
    }

}
