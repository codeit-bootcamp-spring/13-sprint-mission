package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterRepositoryTest {

    private SseEmitterRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SseEmitterRepository();
    }

    @Test
    void save_한_사용자의_여러_연결을_저장한다() {
        UUID receiverId = UUID.randomUUID();
        SseEmitter firstEmitter = new SseEmitter();
        SseEmitter secondEmitter = new SseEmitter();

        SseEmitter firstResult =
                repository.save(receiverId, firstEmitter);
        SseEmitter secondResult =
                repository.save(receiverId, secondEmitter);

        assertThat(firstResult).isSameAs(firstEmitter);
        assertThat(secondResult).isSameAs(secondEmitter);
        assertThat(
                repository.findAllByReceiverId(receiverId)
        ).containsExactly(
                firstEmitter,
                secondEmitter
        );
    }

    @Test
    void save_서로_다른_사용자의_연결을_구분해서_저장한다() {
        UUID firstReceiverId = UUID.randomUUID();
        UUID secondReceiverId = UUID.randomUUID();

        SseEmitter firstEmitter = new SseEmitter();
        SseEmitter secondEmitter = new SseEmitter();

        repository.save(firstReceiverId, firstEmitter);
        repository.save(secondReceiverId, secondEmitter);

        assertThat(
                repository.findAllByReceiverId(firstReceiverId)
        ).containsExactly(firstEmitter);

        assertThat(
                repository.findAllByReceiverId(secondReceiverId)
        ).containsExactly(secondEmitter);
    }

    @Test
    void findAllByReceiverId_연결이_없으면_빈_목록을_반환한다() {
        UUID receiverId = UUID.randomUUID();

        List<SseEmitter> result =
                repository.findAllByReceiverId(receiverId);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_모든_사용자와_연결의_스냅샷을_반환한다() {
        UUID firstReceiverId = UUID.randomUUID();
        UUID secondReceiverId = UUID.randomUUID();

        SseEmitter firstEmitter = new SseEmitter();
        SseEmitter secondEmitter = new SseEmitter();
        SseEmitter thirdEmitter = new SseEmitter();

        repository.save(firstReceiverId, firstEmitter);
        repository.save(firstReceiverId, secondEmitter);
        repository.save(secondReceiverId, thirdEmitter);

        Map<UUID, List<SseEmitter>> result =
                repository.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(firstReceiverId))
                .containsExactly(
                        firstEmitter,
                        secondEmitter
                );
        assertThat(result.get(secondReceiverId))
                .containsExactly(thirdEmitter);
    }

    @Test
    void delete_지정한_연결만_삭제한다() {
        UUID receiverId = UUID.randomUUID();

        SseEmitter firstEmitter = new SseEmitter();
        SseEmitter secondEmitter = new SseEmitter();

        repository.save(receiverId, firstEmitter);
        repository.save(receiverId, secondEmitter);

        repository.delete(receiverId, firstEmitter);

        assertThat(
                repository.findAllByReceiverId(receiverId)
        ).containsExactly(secondEmitter);
    }

    @Test
    void delete_마지막_연결을_삭제하면_사용자_항목도_삭제한다() {
        UUID receiverId = UUID.randomUUID();
        SseEmitter emitter = new SseEmitter();

        repository.save(receiverId, emitter);

        repository.delete(receiverId, emitter);

        assertThat(
                repository.findAllByReceiverId(receiverId)
        ).isEmpty();
        assertThat(repository.findAll())
                .doesNotContainKey(receiverId);
    }

    @Test
    void delete_존재하지_않는_연결을_삭제해도_예외가_발생하지_않는다() {
        UUID receiverId = UUID.randomUUID();
        SseEmitter emitter = new SseEmitter();

        repository.delete(receiverId, emitter);

        assertThat(
                repository.findAllByReceiverId(receiverId)
        ).isEmpty();
    }
}