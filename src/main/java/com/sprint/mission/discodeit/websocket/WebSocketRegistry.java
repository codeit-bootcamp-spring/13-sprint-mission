package com.sprint.mission.discodeit.websocket;

import com.sprint.mission.discodeit.config.redis.RedisLockProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketRegistry {

    private final CopyOnWriteArrayList<String> localSessionList = new CopyOnWriteArrayList<>();
    private final RedisLockProvider redisLockProvider;

    private final StringRedisTemplate redisTemplate;
    private final String sessionPreFix = "session:";
    private final String channelPreFix = "channel:";

    // 현재 활성 리스트에 세션 추가
    public void register(String sessionId, String channelId){
        try {
            redisLockProvider.acquireLock(sessionId);
            redisTemplate.opsForSet().add(channelPreFix + channelId, sessionId);
            redisTemplate.opsForSet().add(sessionPreFix + sessionId, channelId);
            localSessionList.add(sessionId);
            log.debug("[WebSocket] 세션 저장 성공 - session : {}, channel : {}", sessionId, channelId);
        } catch (RedisLockProvider.RedisLockAcquisitionException e) {
            log.error("[WebSocket] 세션 저장 실패 - Redis lock handle fail");
        } finally {
            redisLockProvider.releaseLock(sessionId);
        }
    }

    // 현재 활성 리스트에서 세션 제거
    public void unregister(String sessionId){
        try {
            redisLockProvider.releaseLock(sessionId);
            Set<String> channelList = redisTemplate.opsForSet().members(sessionPreFix + sessionId);
            if (channelList != null && !channelList.isEmpty()){
                for (Object channelId : channelList){
                    redisTemplate.opsForSet().remove(channelPreFix + channelId, sessionId);
                    redisTemplate.opsForSet().remove(sessionPreFix + sessionId, channelId);
                    log.debug("[WebSocket] 세션 삭제 - {}",sessionId);
                }
            }
            localSessionList.remove(sessionId);
        } catch (RedisLockProvider.RedisLockAcquisitionException e) {
            log.error("[WebSocket] 세션 삭제 실패 - Redis lock handle fail");
        } finally {
            redisLockProvider.releaseLock(sessionId);
        }

    }

    public Set<String> joinedSessions(String channelId){
        return redisTemplate.opsForSet().members(channelPreFix + channelId);
    }

    public Set<String> joinedChannelId(String sessionId){
        return redisTemplate.opsForSet().members(sessionPreFix + sessionId);
    }

}
