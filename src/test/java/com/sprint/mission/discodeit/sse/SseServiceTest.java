package com.sprint.mission.discodeit.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

class SseServiceTest {

    private SseService sseService;
    private SseEmitterRepository emitterRepository;
    private SseMessageRepository messageRepository;

    @BeforeEach
    void setUp() {
        emitterRepository = mock(SseEmitterRepository.class);
        messageRepository = mock(SseMessageRepository.class);

        sseService = new SseService(
                emitterRepository,
                messageRepository
        );
    }

    @Test
    @DisplayName("SSE 연결 시 Emitter가 생성되고 저장된다")
    void connect_success() {
        // given
        UUID userId = UUID.randomUUID();

        // when
        SseEmitter emitter = sseService.connect(userId, null);

        // then
        assertThat(emitter).isNotNull();

        then(emitterRepository).should()
                .save(eq(userId), eq(emitter));

        then(messageRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("특정 사용자에게 SSE 이벤트를 저장하고 전송한다")
    void send_success() throws Exception {
        // given
        UUID receiverId = UUID.randomUUID();
        SseEmitter emitter = mock(SseEmitter.class);

        given(emitterRepository.findAllByUserId(receiverId))
                .willReturn(List.of(emitter));

        // when
        sseService.send(
                Set.of(receiverId),
                "notifications.created",
                "새 알림"
        );

        // then
        then(messageRepository).should()
                .save(any(SseMessage.class));

        then(emitter).should()
                .send(any(SseEmitter.SseEventBuilder.class));

        then(emitterRepository).should(never())
                .delete(eq(receiverId), eq(emitter));
    }

    @Test
    @DisplayName("전체 연결 사용자에게 SSE 이벤트를 전송한다")
    void broadcast_success() throws Exception {
        // given
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        SseEmitter emitterA = mock(SseEmitter.class);
        SseEmitter emitterB = mock(SseEmitter.class);

        given(emitterRepository.findAllUserIds())
                .willReturn(Set.of(userA, userB));

        given(emitterRepository.findAllByUserId(userA))
                .willReturn(List.of(emitterA));

        given(emitterRepository.findAllByUserId(userB))
                .willReturn(List.of(emitterB));

        // when
        sseService.broadcast("channels.created", "새 채널");

        // then
        then(emitterA).should()
                .send(any(SseEmitter.SseEventBuilder.class));

        then(emitterB).should()
                .send(any(SseEmitter.SseEventBuilder.class));

        then(messageRepository).should(times(2))
                .save(any(SseMessage.class));
    }

    @Test
    @DisplayName("SSE 재연결 시 마지막 수신 ID 이후의 이벤트를 조회한다")
    void connect_replayEvents() {
        // given
        UUID userId = UUID.randomUUID();
        UUID lastEventId = UUID.randomUUID();

        SseMessage second = new SseMessage(
                UUID.randomUUID(),
                userId,
                "notifications.created",
                "두 번째 알림",
                Instant.now()
        );

        SseMessage third = new SseMessage(
                UUID.randomUUID(),
                userId,
                "notifications.created",
                "세 번째 알림",
                Instant.now()
        );

        given(messageRepository.findAllByUserIdAndIdAfter(
                userId, lastEventId
        )).willReturn(List.of(second, third));

        // when
        SseEmitter emitter = sseService.connect(userId, lastEventId);

        // then
        assertThat(emitter).isNotNull();

        then(emitterRepository).should()
                .save(userId, emitter);

        then(messageRepository).should()
                .findAllByUserIdAndIdAfter(userId, lastEventId);
    }

    @Test
    @DisplayName("SSE 연결 정리 시 전송에 실패한 연결만 삭제한다")
    void cleanUp_removesFailedConnection() throws Exception {
        // given
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        SseEmitter activeEmitter = mock(SseEmitter.class);
        SseEmitter failedEmitter = mock(SseEmitter.class);

        doAnswer(invocation -> {
            BiConsumer<UUID, SseEmitter> action =
                    invocation.getArgument(0);

            action.accept(userA, activeEmitter);
            action.accept(userB, failedEmitter);

            return null;
        }).when(emitterRepository).forEach(any());

        doThrow(new IOException("연결 끊김"))
                .when(failedEmitter)
                .send(any(SseEmitter.SseEventBuilder.class));

        // when
        sseService.cleanUp();

        // then
        then(activeEmitter).should()
                .send(any(SseEmitter.SseEventBuilder.class));

        then(emitterRepository).should()
                .delete(userB, failedEmitter);

        then(emitterRepository).should(never())
                .delete(userA, activeEmitter);
    }
}
