package com.sprint.mission.discodeit.integration;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEventListener;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("BinaryContent 상태 전환 통합 테스트")
class BinaryContentIntegrationTest {

    @Autowired
    private BinaryContentCreatedEventListener binaryContentCreatedEventListener;

    @Autowired
    private BinaryContentRepository binaryContentRepository;

    @MockitoBean
    private BinaryContentStorage binaryContentStorage;

    @BeforeEach
    void setUp() {
        binaryContentRepository.deleteAll();
    }

    @Nested
    @DisplayName("파일 저장 이벤트 처리")
    class HandleBinaryContentCreatedEvent {

        @Test
        @DisplayName("파일 저장에 성공하면 BinaryContent 상태를 SUCCESS로 변경")
        void upload_success() throws Exception {
            // given
            byte[] bytes = "file-data".getBytes();

            BinaryContent binaryContent = binaryContentRepository.save(
                    new BinaryContent(
                            "test.txt",
                            9L,
                            "text/plain"
                    )
            );

            assertThat(binaryContent.getStatus())
                    .isEqualTo(BinaryContentStatus.PROCESSING);

            BinaryContentCreatedEvent event =
                    new BinaryContentCreatedEvent(
                            binaryContent.getId(),
                            bytes
                    );

            // when
            binaryContentCreatedEventListener
                    .onBinaryContentCreated(event);

            // then
            BinaryContent updated = waitForStatus(
                    binaryContent.getId(),
                    BinaryContentStatus.SUCCESS
            );

            assertThat(updated.getStatus())
                    .isEqualTo(BinaryContentStatus.SUCCESS);
        }

        @Test
        @DisplayName("파일 저장에 실패하면 BinaryContent 상태를 FAIL로 변경")
        void upload_fail() throws Exception {
            // given
            byte[] bytes = "file-data".getBytes();

            BinaryContent binaryContent = binaryContentRepository.save(
                    new BinaryContent(
                            "test.txt",
                            9L,
                            "text/plain"
                    )
            );

            willThrow(new RuntimeException("storage failure"))
                    .given(binaryContentStorage)
                    .put(
                            eq(binaryContent.getId()),
                            eq(bytes)
                    );

            BinaryContentCreatedEvent event =
                    new BinaryContentCreatedEvent(
                            binaryContent.getId(),
                            bytes
                    );

            // when
            binaryContentCreatedEventListener
                    .onBinaryContentCreated(event);

            // then
            BinaryContent updated = waitForStatus(
                    binaryContent.getId(),
                    BinaryContentStatus.FAIL
            );

            assertThat(updated.getStatus())
                    .isEqualTo(BinaryContentStatus.FAIL);
        }
    }

    private BinaryContent waitForStatus(
            UUID binaryContentId,
            BinaryContentStatus expectedStatus
    ) throws InterruptedException {

        for (int i = 0; i < 20; i++) {
            BinaryContent binaryContent =
                    binaryContentRepository
                            .findById(binaryContentId)
                            .orElseThrow();

            if (binaryContent.getStatus() == expectedStatus) {
                return binaryContent;
            }

            Thread.sleep(100);
        }

        throw new AssertionError(
                "BinaryContent 상태가 "
                        + expectedStatus
                        + "로 변경되지 않았습니다."
        );
    }
}