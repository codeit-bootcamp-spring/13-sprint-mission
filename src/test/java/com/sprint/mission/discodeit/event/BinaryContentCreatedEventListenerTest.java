package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BinaryContentCreatedEventListenerTest {

    @Mock
    private BinaryContentStorage binaryContentStorage;

    @Mock
    private BinaryContentService binaryContentService;

    @InjectMocks
    private BinaryContentCreatedEventListener listener;

    @Test
    void handle_파일_업로드에_성공하면_상태를_SUCCESS로_변경한다() {
        UUID binaryContentId = UUID.randomUUID();
        byte[] bytes = "test-content".getBytes();

        BinaryContentCreatedEvent event =
                new BinaryContentCreatedEvent(binaryContentId, bytes);

        given(binaryContentStorage.put(binaryContentId, bytes))
                .willReturn(binaryContentId);

        listener.handle(event);

        then(binaryContentStorage).should()
                .put(binaryContentId, bytes);
        then(binaryContentService).should()
                .updateStatus(
                        binaryContentId,
                        BinaryContentStatus.SUCCESS
                );
    }

    @Test
    void handle_파일_업로드에_실패하면_상태를_FAIL로_변경한다() {
        UUID binaryContentId = UUID.randomUUID();
        byte[] bytes = "test-content".getBytes();

        BinaryContentCreatedEvent event =
                new BinaryContentCreatedEvent(binaryContentId, bytes);

        given(binaryContentStorage.put(binaryContentId, bytes))
                .willThrow(new RuntimeException("업로드 실패"));

        listener.handle(event);

        then(binaryContentStorage).should()
                .put(binaryContentId, bytes);
        then(binaryContentService).should()
                .updateStatus(
                        binaryContentId,
                        BinaryContentStatus.FAIL
                );
    }
}