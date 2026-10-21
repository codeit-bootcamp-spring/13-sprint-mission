package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.event.ChannelSseEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

class ChannelSseEventListenerTest {

    @Test
    @DisplayName("공개 채널 이벤트는 모든 SSE 연결 사용자에게 브로드캐스트한다")
    void handleChannelChanged_broadcast() {
        // given
        SseService sseService = mock(SseService.class);

        ChannelSseEventListener listener =
                new ChannelSseEventListener(sseService);

        ChannelDto channel = mock(ChannelDto.class);

        ChannelSseEvent event = new ChannelSseEvent(
                "channels.created",
                channel,
                Set.of(),
                true
        );

        // when
        listener.handleChannelChanged(event);

        // then
        then(sseService).should()
                .broadcast("channels.created", channel);

        then(sseService).should(never())
                .send(any(Set.class), anyString(), any());
    }

    @Test
    @DisplayName("비공개 채널 이벤트는 참여자에게만 SSE로 전송한다")
    void handleChannelChanged_privateChannel() {
        // given
        SseService sseService = mock(SseService.class);

        ChannelSseEventListener listener =
                new ChannelSseEventListener(sseService);

        ChannelDto channel = mock(ChannelDto.class);

        UUID receiverId1 = UUID.randomUUID();
        UUID receiverId2 = UUID.randomUUID();

        Set<UUID> receiverIds =
                Set.of(receiverId1, receiverId2);

        ChannelSseEvent event = new ChannelSseEvent(
                "channels.updated",
                channel,
                receiverIds,
                false
        );

        // when
        listener.handleChannelChanged(event);

        // then
        then(sseService).should()
                .send(receiverIds, "channels.updated", channel);

        then(sseService).should(never())
                .broadcast(anyString(), any());
    }
}
