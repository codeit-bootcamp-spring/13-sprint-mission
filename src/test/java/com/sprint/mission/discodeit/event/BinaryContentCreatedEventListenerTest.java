package com.sprint.mission.discodeit.event;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BinaryContentCreatedEventListenerTest {

    @Mock
    private BinaryContentStorage binaryContentStorage;

    @Mock
    private BinaryContentService binaryContentService;

    @InjectMocks
    private BinaryContentCreatedEventListener listener;

    @Test
    void shouldUpdateStatusToSuccessWhenBinaryContentStorageSucceeds() {
        // given
        UUID binaryContentId = UUID.randomUUID();
        byte[] bytes = "test".getBytes(StandardCharsets.UTF_8);

        BinaryContentCreatedEvent event =
                new BinaryContentCreatedEvent(binaryContentId, bytes);

        // when
        listener.onBinaryContentCreated(event);

        // then
        verify(binaryContentStorage).put(binaryContentId, bytes);
        verify(binaryContentService).updateStatus(
                binaryContentId,
                BinaryContentStatus.SUCCESS
        );
        verify(binaryContentService, never()).updateStatus(
                binaryContentId,
                BinaryContentStatus.FAIL
        );
    }

    @Test
    void shouldUpdateStatusToFailWhenBinaryContentStorageFails() {
        // given
        UUID binaryContentId = UUID.randomUUID();
        byte[] bytes = "test".getBytes(StandardCharsets.UTF_8);

        BinaryContentCreatedEvent event =
                new BinaryContentCreatedEvent(binaryContentId, bytes);

        doThrow(new RuntimeException("파일 저장 실패"))
                .when(binaryContentStorage)
                .put(binaryContentId, bytes);

        // when
        listener.onBinaryContentCreated(event);

        // then
        verify(binaryContentStorage).put(binaryContentId, bytes);
        verify(binaryContentService).updateStatus(
                binaryContentId,
                BinaryContentStatus.FAIL
        );
        verify(binaryContentService, never()).updateStatus(
                binaryContentId,
                BinaryContentStatus.SUCCESS
        );
    }
}