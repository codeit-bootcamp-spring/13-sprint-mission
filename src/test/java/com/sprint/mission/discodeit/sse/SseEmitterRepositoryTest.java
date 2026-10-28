package com.sprint.mission.discodeit.sse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SseEmitterRepositoryTest {

    private SseEmitterRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SseEmitterRepository();
    }

    @Test
    @DisplayName("SSE 연결을 저장하고 조회할 수 있다")
    void save_success() {
        // given
        UUID userId = UUID.randomUUID();
        SseEmitter emitter = new SseEmitter();

        // when
        repository.save(userId, emitter);

        // then
        assertThat(repository.findAllByUserId(userId))
                .containsExactly(emitter);

        assertThat(repository.findAllUserIds())
                .containsExactly(userId);
    }

    @Test
    @DisplayName("한 사용자가 여러 SSE 연결을 가질 수 있다")
    void save_multipleConnections() {
        // given
        UUID userId = UUID.randomUUID();
        SseEmitter firstEmitter = new SseEmitter();
        SseEmitter secondEmitter = new SseEmitter();

        // when
        repository.save(userId, firstEmitter);
        repository.save(userId, secondEmitter);

        // then
        assertThat(repository.findAllByUserId(userId))
                .containsExactly(firstEmitter, secondEmitter);

        assertThat(repository.findAllUserIds())
                .hasSize(1);
    }

    @Test
    @DisplayName("특정 SSE 연결만 삭제할 수 있다")
    void delete_oneConnection() {
        // given
        UUID userId = UUID.randomUUID();
        SseEmitter firstEmitter = new SseEmitter();
        SseEmitter secondEmitter = new SseEmitter();

        repository.save(userId, firstEmitter);
        repository.save(userId, secondEmitter);

        // when
        repository.delete(userId, firstEmitter);

        // then
        assertThat(repository.findAllByUserId(userId))
                .containsExactly(secondEmitter);

        assertThat(repository.findAllUserIds())
                .contains(userId);
    }

    @Test
    @DisplayName("마지막 SSE 연결을 삭제하면 사용자 정보도 제거된다")
    void delete_lastConnection() {
        // given
        UUID userId = UUID.randomUUID();
        SseEmitter emitter = new SseEmitter();

        repository.save(userId, emitter);

        // when
        repository.delete(userId, emitter);

        // then
        assertThat(repository.findAllByUserId(userId))
                .isEmpty();

        assertThat(repository.findAllUserIds())
                .doesNotContain(userId);
    }
}
