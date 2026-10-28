package com.sprint.mission.discodeit.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static com.sprint.mission.discodeit.sse.SseTestFixtures.message;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 실제 인메모리 저장소로 중복 ID, 수신 대상 필터, 이력 순서와 동시 저장을 검증한다.
 * 발행자 Mock의 콜백에서 저장 결과를 조회하여 발행 시점에 순번과 본문이 준비되어 있는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("실제 인메모리 SSE 메시지 저장소 단위 테스트")
class InMemorySseMessageRepositoryTest {
    @Mock ApplicationEventPublisher eventPublisher;
    private InMemorySseMessageRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySseMessageRepository(eventPublisher);
    }

    @Test
    @DisplayName("이벤트를 받는 시점에는 메시지와 순번을 이미 조회할 수 있다")
    void saveAndPublish_storesBeforePublishing() {
        UUID receiver = UUID.randomUUID();
        SseMessage cursor = message(receiver);
        repository.saveAndPublish(cursor);
        SseMessage next = message(receiver);
        doAnswer(invocation -> {
            assertThat(repository.findSequence(next.eventId())).hasValue(2);
            assertThat(repository.findAllAfter(receiver, cursor.eventId())).containsExactly(next);
            return null;
        }).when(eventPublisher).publishEvent(next);

        assertThat(repository.saveAndPublish(next)).isTrue();

        verify(eventPublisher).publishEvent(next);
    }

    @Test
    @DisplayName("중복 ID는 내용과 순번을 덮어쓰거나 다시 발행하지 않는다")
    void saveAndPublish_rejectsDuplicateWithoutMutatingHistory() {
        UUID receiver = UUID.randomUUID();
        SseMessage cursor = message(receiver);
        SseMessage original = message(receiver);
        repository.saveAndPublish(cursor);
        repository.saveAndPublish(original);
        SseMessage duplicate = new SseMessage(original.eventId(), "users.deleted", "{}",
                Set.of(), true);

        assertThat(repository.saveAndPublish(duplicate)).isFalse();
        SseMessage next = message(receiver);
        assertThat(repository.saveAndPublish(next)).isTrue();

        assertThat(repository.findSequence(original.eventId())).hasValue(2);
        assertThat(repository.findSequence(next.eventId())).hasValue(3);
        assertThat(repository.findAllAfter(receiver, cursor.eventId())).containsExactly(original, next);
        verify(eventPublisher).publishEvent(original);
        verify(eventPublisher, never()).publishEvent(duplicate);
    }

    @Test
    @DisplayName("커서 이후 메시지를 수신 대상과 전체 방송 여부로 필터링하고 저장 순서로 반환한다")
    void findAllAfter_filtersAudienceAndExcludesCursor() {
        UUID receiver = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        SseMessage cursor = message(receiver);
        SseMessage targeted = message(receiver);
        SseMessage shared = new SseMessage(UUID.randomUUID(), "channels.updated", "{}",
                Set.of(receiver, other), false);
        SseMessage otherOnly = message(other);
        SseMessage broadcast = new SseMessage(UUID.randomUUID(), "users.updated", "{}", Set.of(), true);
        List.of(cursor, targeted, shared, otherOnly, broadcast).forEach(repository::saveAndPublish);

        assertThat(repository.findAllAfter(receiver, cursor.eventId())).containsExactly(targeted, shared, broadcast);
        assertThat(repository.findAllAfter(other, cursor.eventId())).containsExactly(shared, otherOnly, broadcast);
        // 연결 정보가 없는 다른 사용자도 전체 방송 이력을 복원할 수 있다.
        assertThat(repository.findAllAfter(UUID.randomUUID(), cursor.eventId())).containsExactly(broadcast);
    }

    @Test
    @DisplayName("커서가 없거나 알려지지 않은 커서는 빈 이력을 반환한다")
    void findAllAfter_returnsEmptyForMissingOrUnknownCursor() {
        UUID receiver = UUID.randomUUID();
        repository.saveAndPublish(message(receiver));
        UUID unknown = UUID.randomUUID();

        assertThat(repository.findAllAfter(receiver, null)).isEmpty();
        assertThat(repository.findAllAfter(receiver, unknown)).isEmpty();
        assertThat(repository.findSequence(null)).isEmpty();
        assertThat(repository.findSequence(unknown)).isEmpty();
    }

    @Test
    @DisplayName("조회 결과는 이후 저장과 분리된 수정 불가능한 목록이다")
    void findAllAfter_returnsImmutableSnapshot() {
        UUID receiver = UUID.randomUUID();
        SseMessage cursor = message(receiver);
        SseMessage first = message(receiver);
        repository.saveAndPublish(cursor);
        repository.saveAndPublish(first);
        List<SseMessage> snapshot = repository.findAllAfter(receiver, cursor.eventId());

        repository.saveAndPublish(message(receiver));

        assertThat(snapshot).containsExactly(first);
        assertThatThrownBy(snapshot::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("같은 ID를 동시에 저장해도 한 요청만 성공하고 한 번만 발행한다")
    void saveAndPublish_acceptsOnlyOneConcurrentDuplicate() throws Exception {
        SseMessage message = message(UUID.randomUUID());

        List<Boolean> results = concurrently(List.of(
                () -> repository.saveAndPublish(message),
                () -> repository.saveAndPublish(message)));

        assertThat(results).containsExactlyInAnyOrder(true, false);
        assertThat(repository.findSequence(message.eventId())).hasValue(1);
        verify(eventPublisher).publishEvent(message);
    }

    @Test
    @DisplayName("동시 저장에서도 순번이 유일하며 저장 순서대로 이벤트를 발행한다")
    void saveAndPublish_preservesPublicationOrderUnderConcurrency() throws Exception {
        UUID receiver = UUID.randomUUID();
        SseMessage cursor = message(receiver);
        repository.saveAndPublish(cursor);
        List<SseMessage> messages = IntStream.range(0, 12).mapToObj(i -> message(receiver)).toList();
        List<UUID> publishedIds = new CopyOnWriteArrayList<>();
        List<Long> publishedSequences = new CopyOnWriteArrayList<>();
        doAnswer(invocation -> {
            SseMessage published = invocation.getArgument(0);
            publishedIds.add(published.eventId());
            publishedSequences.add(repository.findSequence(published.eventId()).orElseThrow());
            return null;
        }).when(eventPublisher).publishEvent(any(SseMessage.class));

        List<Boolean> results = concurrently(messages.stream()
                .<Callable<Boolean>>map(message -> () -> repository.saveAndPublish(message)).toList());

        assertThat(results).containsOnly(true).hasSize(messages.size());
        assertThat(publishedIds).containsExactlyInAnyOrderElementsOf(messages.stream().map(SseMessage::eventId).toList());
        assertThat(publishedSequences).containsExactlyElementsOf(LongStream.rangeClosed(2, 13).boxed().toList());
        assertThat(repository.findAllAfter(receiver, cursor.eventId()))
                .extracting(SseMessage::eventId).containsExactlyElementsOf(publishedIds);
    }

    private List<Boolean> concurrently(List<Callable<Boolean>> tasks) throws Exception {
        CountDownLatch ready = new CountDownLatch(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(tasks.size());
        List<Future<Boolean>> futures = new ArrayList<>();
        try {
            for (Callable<Boolean> task : tasks) {
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(5, SECONDS)) throw new IllegalStateException("start timeout");
                    return task.call();
                }));
            }
            assertThat(ready.await(5, SECONDS)).isTrue();
            start.countDown();
            List<Boolean> results = new ArrayList<>();
            for (Future<Boolean> future : futures) results.add(future.get(5, SECONDS));
            return results;
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, SECONDS)).isTrue();
        }
    }
}
