package com.sprint.mission.discodeit.security.jwt;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
@Slf4j
public class InMemoryJwtRegistry implements JwtRegistry{

    private final String SERVICE_NAME = "JwtRegistry";

    // 나중에 concurrent linked queue 로 변경?
    private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
    // 최대 동시 사용자 1명.
    private final int maxActiveJwtCount;

    private final JwtTokenProvider tokenProvider;

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

        log.debug("[{}] - jwt 토큰 등록 - user : {}",SERVICE_NAME,userId);
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        Queue<JwtInformation> queue = getQueueFromUserId(userId);

        queue.clear();
        log.debug("[{}] - jwt 토큰 전체 삭제 - user : {}",SERVICE_NAME,userId);
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        Queue<JwtInformation> queue = getQueueFromUserId(userId);
        return !queue.isEmpty();
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String token) {
        return origin.values().stream().anyMatch(
                q -> (q.stream()
                        .anyMatch(
                                info -> info.getAccessToken().equals(token)
                        ))
        );
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String token) {
        return origin.values().stream().anyMatch(
                q -> (q.stream()
                        .anyMatch(
                                info -> info.getRefreshToken().equals(token)
                        ))
        );
    }

    /**
     * refresh 토큰으로 저장소의 토큰을 검사하고, 새 JwtInformation 으로 등록을 치환.
     * @param refresh
     * @param newInfo 새 JwtInformation.
     */
    @Override
    public void rotateJwtInformation(String refresh, JwtInformation newInfo) {
        /*
        refresh 체크, 리프레쉬가 동일하다면 엑세스 토큰 변경.
         */

        UUID userId = UUID.fromString(tokenProvider.getSubject(refresh));

        Queue<JwtInformation> storage = origin.get(userId);

        storage.stream()
                .filter(info -> info.getRefreshToken().equals(refresh))
                .forEach(info -> info.rotate(newInfo.getAccessToken(),  newInfo.getRefreshToken()));

        log.debug("[{}] - jwt 토큰 로테이트 - user : {}",SERVICE_NAME,userId);
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
                       if(tokenProvider.isExpired(info.getRefreshToken()))
                           q.remove(info);
                    }
            );
        }
    }

}
