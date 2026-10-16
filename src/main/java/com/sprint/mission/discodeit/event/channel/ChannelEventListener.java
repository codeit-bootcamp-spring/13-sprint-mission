package com.sprint.mission.discodeit.event.channel;

import com.sprint.mission.discodeit.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChannelEventListener {

    private final String NAME = "ChannelEventListener";

    private final SseService sseService;

    // 채널 정보 변경에 대한 이벤트 함수. 변경점이 커밋되고 난 후, 커밋을 보낸다.
    // todo - 알림 대상을 어떻게 분간? 채널에 따라 접속할 수 있는 유저를 파악해서 알림?
    @Async("notification")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelEvent channelEvent) {
        log.debug("[{}] - 채널 이벤트 실행",NAME);

        sseService.broadcast(channelEvent.getName(), channelEvent.getData());
    }
}
