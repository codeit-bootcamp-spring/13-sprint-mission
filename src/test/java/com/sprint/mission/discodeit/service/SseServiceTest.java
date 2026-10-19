package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import com.sprint.mission.discodeit.sse.SseMessage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class SseServiceTest {

    @Mock
    private SseEmitterRepository sseEmitterRepository;

    @Mock
    private SseMessageRepository sseMessageRepository;

    @InjectMocks
    private SseService sseService;

    @Test
    void connect_연결을_저장하고_누락된_이벤트를_조회한다() {
        UUID receiverId = UUID.randomUUID();
        UUID lastEventId = UUID.randomUUID();

        SseMessage missedMessage = new SseMessage(
                UUID.randomUUID(),
                Set.of(receiverId),
                "notifications.created",
                "missed-message"
        );

        given(
                sseMessageRepository.findAllAfter(
                        lastEventId,
                        receiverId
                )
        ).willReturn(List.of(missedMessage));

        SseEmitter result =
                sseService.connect(
                        receiverId,
                        lastEventId
                );

        assertThat(result).isNotNull();
        assertThat(result.getTimeout())
                .isEqualTo(1000L * 60 * 60);

        then(sseEmitterRepository).should()
                .save(
                        eq(receiverId),
                        same(result)
                );
        then(sseMessageRepository).should()
                .findAllAfter(
                        lastEventId,
                        receiverId
                );
    }

    @Test
    void connect_마지막_이벤트_ID가_없어도_연결한다() {
        UUID receiverId = UUID.randomUUID();

        given(
                sseMessageRepository.findAllAfter(
                        null,
                        receiverId
                )
        ).willReturn(List.of());

        SseEmitter result =
                sseService.connect(receiverId, null);

        assertThat(result).isNotNull();

        then(sseEmitterRepository).should()
                .save(
                        eq(receiverId),
                        same(result)
                );
        then(sseMessageRepository).should()
                .findAllAfter(null, receiverId);
    }

    @Test
    void send_지정된_사용자의_모든_연결에_이벤트를_전송한다()
            throws Exception {
        UUID firstReceiverId = UUID.randomUUID();
        UUID secondReceiverId = UUID.randomUUID();

        SseEmitter firstEmitter = org.mockito.Mockito.mock(
                SseEmitter.class
        );
        SseEmitter secondEmitter = org.mockito.Mockito.mock(
                SseEmitter.class
        );
        SseEmitter thirdEmitter = org.mockito.Mockito.mock(
                SseEmitter.class
        );

        String eventName = "notifications.created";
        Object data = new Object();

        Set<UUID> receiverIds = Set.of(
                firstReceiverId,
                secondReceiverId
        );

        SseMessage message = new SseMessage(
                UUID.randomUUID(),
                receiverIds,
                eventName,
                data
        );

        given(
                sseMessageRepository.save(
                        receiverIds,
                        eventName,
                        data
                )
        ).willReturn(message);

        given(
                sseEmitterRepository.findAllByReceiverId(
                        firstReceiverId
                )
        ).willReturn(
                List.of(
                        firstEmitter,
                        secondEmitter
                )
        );

        given(
                sseEmitterRepository.findAllByReceiverId(
                        secondReceiverId
                )
        ).willReturn(List.of(thirdEmitter));

        sseService.send(
                receiverIds,
                eventName,
                data
        );

        then(firstEmitter).should()
                .send(
                        any(
                                SseEmitter.SseEventBuilder.class
                        )
                );
        then(secondEmitter).should()
                .send(
                        any(
                                SseEmitter.SseEventBuilder.class
                        )
                );
        then(thirdEmitter).should()
                .send(
                        any(
                                SseEmitter.SseEventBuilder.class
                        )
                );
    }

    @Test
    void send_전송에_실패한_연결을_삭제한다()
            throws Exception {
        UUID receiverId = UUID.randomUUID();
        SseEmitter emitter = org.mockito.Mockito.mock(
                SseEmitter.class
        );

        String eventName = "notifications.created";
        Object data = new Object();

        Set<UUID> receiverIds = Set.of(receiverId);

        SseMessage message = new SseMessage(
                UUID.randomUUID(),
                receiverIds,
                eventName,
                data
        );

        given(
                sseMessageRepository.save(
                        receiverIds,
                        eventName,
                        data
                )
        ).willReturn(message);

        given(
                sseEmitterRepository.findAllByReceiverId(
                        receiverId
                )
        ).willReturn(List.of(emitter));

        willThrow(new IOException("send failed"))
                .given(emitter)
                .send(
                        any(
                                SseEmitter.SseEventBuilder.class
                        )
                );

        sseService.send(
                receiverIds,
                eventName,
                data
        );

        then(emitter).should().complete();
        then(sseEmitterRepository).should()
                .delete(receiverId, emitter);
    }

    @Test
    void broadcast_모든_사용자의_모든_연결에_이벤트를_전송한다()
            throws Exception {
        UUID firstReceiverId = UUID.randomUUID();
        UUID secondReceiverId = UUID.randomUUID();

        SseEmitter firstEmitter = org.mockito.Mockito.mock(
                SseEmitter.class
        );
        SseEmitter secondEmitter = org.mockito.Mockito.mock(
                SseEmitter.class
        );

        String eventName = "users.updated";
        Object data = new Object();

        SseMessage message = new SseMessage(
                UUID.randomUUID(),
                Set.of(),
                eventName,
                data
        );

        given(
                sseMessageRepository.save(
                        List.of(),
                        eventName,
                        data
                )
        ).willReturn(message);

        given(sseEmitterRepository.findAll())
                .willReturn(
                        Map.of(
                                firstReceiverId,
                                List.of(firstEmitter),
                                secondReceiverId,
                                List.of(secondEmitter)
                        )
                );

        sseService.broadcast(eventName, data);

        then(firstEmitter).should()
                .send(
                        any(
                                SseEmitter.SseEventBuilder.class
                        )
                );
        then(secondEmitter).should()
                .send(
                        any(
                                SseEmitter.SseEventBuilder.class
                        )
                );
    }

    @Test
    void broadcast_전송에_실패한_연결을_삭제한다()
            throws Exception {
        UUID receiverId = UUID.randomUUID();
        SseEmitter emitter = org.mockito.Mockito.mock(
                SseEmitter.class
        );

        String eventName = "users.updated";
        Object data = new Object();

        SseMessage message = new SseMessage(
                UUID.randomUUID(),
                Set.of(),
                eventName,
                data
        );

        given(
                sseMessageRepository.save(
                        List.of(),
                        eventName,
                        data
                )
        ).willReturn(message);

        given(sseEmitterRepository.findAll())
                .willReturn(
                        Map.of(
                                receiverId,
                                List.of(emitter)
                        )
                );

        willThrow(new IOException("send failed"))
                .given(emitter)
                .send(
                        any(
                                SseEmitter.SseEventBuilder.class
                        )
                );

        sseService.broadcast(eventName, data);

        then(emitter).should().complete();
        then(sseEmitterRepository).should()
                .delete(receiverId, emitter);
    }

    @Test
    void cleanUp_정상적인_연결에는_ping을_전송한다()
            throws Exception {
        UUID receiverId = UUID.randomUUID();
        SseEmitter emitter = org.mockito.Mockito.mock(
                SseEmitter.class
        );

        given(sseEmitterRepository.findAll())
                .willReturn(
                        Map.of(
                                receiverId,
                                List.of(emitter)
                        )
                );

        sseService.cleanUp();

        then(emitter).should()
                .send(
                        any(
                                SseEmitter.SseEventBuilder.class
                        )
                );
        then(sseEmitterRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void cleanUp_ping_전송에_실패한_연결을_삭제한다()
            throws Exception {
        UUID receiverId = UUID.randomUUID();
        SseEmitter emitter = org.mockito.Mockito.mock(
                SseEmitter.class
        );

        given(sseEmitterRepository.findAll())
                .willReturn(
                        Map.of(
                                receiverId,
                                List.of(emitter)
                        )
                );

        willThrow(new IOException("ping failed"))
                .given(emitter)
                .send(
                        any(
                                SseEmitter.SseEventBuilder.class
                        )
                );

        sseService.cleanUp();

        then(emitter).should().complete();
        then(sseEmitterRepository).should()
                .delete(receiverId, emitter);
    }
}