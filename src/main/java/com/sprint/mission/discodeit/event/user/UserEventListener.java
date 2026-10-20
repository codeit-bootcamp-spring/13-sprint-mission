package com.sprint.mission.discodeit.event.user;


import com.sprint.mission.discodeit.config.cache.CacheConfig;
import com.sprint.mission.discodeit.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final SseService sseService;

    @Async("notificationExecutor")
    @EventListener
    @CacheEvict(cacheNames = CacheConfig.USER_CACHE, allEntries = true) // 작동을 하나?
    public void on(UserEvent event) {
        log.debug("[UserEvent] {} 이벤트 - {}({})",
                event.getName(),
                event.getData().username(),
                event.getData().id());

        sseService.broadcast(event.getName(), event.getData());
    }

}
