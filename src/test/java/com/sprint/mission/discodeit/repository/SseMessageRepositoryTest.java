package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.sse.SseMessage;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SseMessageRepositoryTest {

    private SseMessageRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SseMessageRepository();
    }

    @Test
    void save_수신자와_이벤트_정보를_저장한다() {
        UUID firstReceiverId = UUID.randomUUID();
        UUID secondReceiverId = UUID.randomUUID();
        Object data = new Object();

        SseMessage result = repository.save(
                List.of(
                        firstReceiverId,
                        secondReceiverId
                ),
                "users.updated",
                data
        );

        assertThat(result.id()).isNotNull();
        assertThat(result.receiverIds())
                .containsExactlyInAnyOrder(
                        firstReceiverId,
                        secondReceiverId
                );
        assertThat(result.eventName())
                .isEqualTo("users.updated");
        assertThat(result.data()).isSameAs(data);
        assertThat(result.canReceive(firstReceiverId))
                .isTrue();
        assertThat(result.canReceive(secondReceiverId))
                .isTrue();
    }

    @Test
    void save_수신자가_비어있으면_전체_사용자용_메시지가_된다() {
        Object data = new Object();

        SseMessage result = repository.save(
                List.of(),
                "channels.created",
                data
        );

        assertThat(result.receiverIds()).isEmpty();
        assertThat(result.canReceive(UUID.randomUUID()))
                .isTrue();
    }

    @Test
    void findAllAfter_마지막_이벤트_이후의_수신_가능한_메시지만_반환한다() {
        UUID receiverId = UUID.randomUUID();
        UUID otherReceiverId = UUID.randomUUID();

        SseMessage lastReceivedMessage = repository.save(
                List.of(receiverId),
                "users.updated",
                "last-received"
        );

        repository.save(
                List.of(otherReceiverId),
                "notifications.created",
                "other-user-message"
        );

        SseMessage broadcastMessage = repository.save(
                List.of(),
                "channels.updated",
                "broadcast-message"
        );

        SseMessage receiverMessage = repository.save(
                List.of(receiverId),
                "notifications.created",
                "receiver-message"
        );

        List<SseMessage> result =
                repository.findAllAfter(
                        lastReceivedMessage.id(),
                        receiverId
                );

        assertThat(result).containsExactly(
                broadcastMessage,
                receiverMessage
        );
    }

    @Test
    void findAllAfter_마지막_이벤트_자체는_반환하지_않는다() {
        UUID receiverId = UUID.randomUUID();

        SseMessage lastReceivedMessage = repository.save(
                List.of(receiverId),
                "users.updated",
                "last-received"
        );

        List<SseMessage> result =
                repository.findAllAfter(
                        lastReceivedMessage.id(),
                        receiverId
                );

        assertThat(result).isEmpty();
    }

    @Test
    void findAllAfter_마지막_이벤트_ID가_null이면_빈_목록을_반환한다() {
        UUID receiverId = UUID.randomUUID();

        repository.save(
                List.of(receiverId),
                "users.updated",
                "message"
        );

        List<SseMessage> result =
                repository.findAllAfter(
                        null,
                        receiverId
                );

        assertThat(result).isEmpty();
    }

    @Test
    void findAllAfter_존재하지_않는_이벤트_ID이면_빈_목록을_반환한다() {
        UUID receiverId = UUID.randomUUID();

        repository.save(
                List.of(receiverId),
                "users.updated",
                "message"
        );

        List<SseMessage> result =
                repository.findAllAfter(
                        UUID.randomUUID(),
                        receiverId
                );

        assertThat(result).isEmpty();
    }

    @Test
    void findAllAfter_메시지의_저장_순서를_유지한다() {
        UUID receiverId = UUID.randomUUID();

        SseMessage lastReceivedMessage = repository.save(
                List.of(receiverId),
                "users.updated",
                "last-received"
        );

        SseMessage firstMessage = repository.save(
                List.of(receiverId),
                "users.updated",
                "first"
        );

        SseMessage secondMessage = repository.save(
                List.of(receiverId),
                "users.updated",
                "second"
        );

        SseMessage thirdMessage = repository.save(
                List.of(receiverId),
                "users.updated",
                "third"
        );

        List<SseMessage> result =
                repository.findAllAfter(
                        lastReceivedMessage.id(),
                        receiverId
                );

        assertThat(result).containsExactly(
                firstMessage,
                secondMessage,
                thirdMessage
        );
    }
}