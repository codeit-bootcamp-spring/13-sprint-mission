package com.sprint.mission.discodeit.event.channel;

import com.sprint.mission.discodeit.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChannelEventListener {

    private final String NAME = "ChannelEventListener";

    private final SseService sseService;

    // 채널 정보 변경에 대한 이벤트 함수. 변경점이 커밋되고 난 후, 커밋을 보낸다.
    // todo - 알림 대상을 어떻게 분간? 채널에 따라 접속할 수 있는 유저를 파악해서 알림?
    @Async("notificationExecutor")
    @EventListener
    public void on(ChannelEvent channelEvent) {
        log.debug("[{}] - 채널 이벤트 실행",NAME);

        sseService.broadcast(channelEvent.getName(), channelEvent.getData());
    }
}
