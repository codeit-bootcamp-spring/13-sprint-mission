package com.sprint.mission.discodeit.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SseMessageRepositoryTest {

    private SseMessageRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SseMessageRepository();
    }

    @Test
    @DisplayName("SSE 이벤트를 저장하고 조회할 수 있다")
    void save_success() {
        // given
        UUID userId = UUID.randomUUID();

        SseMessage first = new SseMessage(
                UUID.randomUUID(),
                userId,
                "connected",
                "연결 완료",
                Instant.now()
        );

        SseMessage message = new SseMessage(
                UUID.randomUUID(),
                userId,
                "notifications.created",
                "새로운 알림",
                Instant.now()
        );

        // when
        repository.save(first);
        repository.save(message);

        // then
        List<SseMessage> result =
                repository.findAllByUserIdAndIdAfter(
                        userId, first.id()
                );

        assertThat(result).containsExactly(message);
    }

    @Test
    @DisplayName("다른 사용자의 이벤트는 조회되지 않는다")
    void findAll_onlyTargetUser() {
        // given
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        SseMessage first = new SseMessage(
                UUID.randomUUID(),
                userA,
                "connected",
                "연결 완료",
                Instant.now()
        );

        SseMessage messageA = new SseMessage(
                UUID.randomUUID(),
                userA,
                "notifications.created",
                "A의 알림",
                Instant.now()
        );

        SseMessage messageB = new SseMessage(
                UUID.randomUUID(),
                userB,
                "notifications.created",
                "B의 알림",
                Instant.now()
        );

        repository.save(first);
        repository.save(messageB);
        repository.save(messageA);

        // when
        List<SseMessage> result =
                repository.findAllByUserIdAndIdAfter(
                        userA, first.id()
                );

        // then
        assertThat(result).containsExactly(messageA);
    }

    @Test
    @DisplayName("마지막 수신 ID 이후의 이벤트를 저장 순서대로 조회한다")
    void findAll_afterLastEventId() {
        // given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        SseMessage first = new SseMessage(
                UUID.randomUUID(),
                userId,
                "users.created",
                "첫 번째",
                now
        );

        SseMessage second = new SseMessage(
                UUID.randomUUID(),
                userId,
                "users.updated",
                "두 번째",
                now
        );

        SseMessage third = new SseMessage(
                UUID.randomUUID(),
                userId,
                "users.deleted",
                "세 번째",
                now
        );

        repository.save(first);
        repository.save(second);
        repository.save(third);

        // when
        List<SseMessage> result =
                repository.findAllByUserIdAndIdAfter(
                        userId, first.id()
                );

        // then
        assertThat(result).containsExactly(second, third);
    }

    @Test
    @DisplayName("보관 기간이 지난 이벤트만 삭제한다")
    void deleteBefore_success() {
        // given
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        SseMessage oldMessage = new SseMessage(
                UUID.randomUUID(),
                userId,
                "notifications.created",
                "오래된 알림",
                now.minus(2, ChronoUnit.HOURS)
        );

        SseMessage recentMessage = new SseMessage(
                UUID.randomUUID(),
                userId,
                "notifications.created",
                "최근 알림",
                now
        );

        SseMessage latestMessage = new SseMessage(
                UUID.randomUUID(),
                userId,
                "notifications.created",
                "최신 알림",
                now
        );

        repository.save(oldMessage);
        repository.save(recentMessage);
        repository.save(latestMessage);

        // when
        repository.deleteBefore(
                now.minus(1, ChronoUnit.HOURS)
        );

        // then
        List<SseMessage> result =
                repository.findAllByUserIdAndIdAfter(
                        userId, recentMessage.id()
                );

        assertThat(result).containsExactly(latestMessage);

        List<SseMessage> afterDeletedId =
                repository.findAllByUserIdAndIdAfter(
                        userId, oldMessage.id()
                );

        assertThat(afterDeletedId).isEmpty();
    }
}
