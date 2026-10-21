package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.event.BinaryContentUpdatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class BinaryContentSseEventListenerTest {

    @Test
    @DisplayName("파일 상태 변경 이벤트를 수신하면 SSE로 브로드캐스트한다")
    void handleBinaryContentUpdated_success() {
        // given
        SseService sseService = mock(SseService.class);

        BinaryContentSseEventListener listener =
                new BinaryContentSseEventListener(sseService);

        UUID binaryContentId = UUID.randomUUID();

        BinaryContentDto binaryContent = mock(BinaryContentDto.class);
        given(binaryContent.id()).willReturn(binaryContentId);

        BinaryContentUpdatedEvent event =
                new BinaryContentUpdatedEvent(binaryContent);

        // when
        listener.handleBinaryContentUpdated(event);

        // then
        then(sseService).should()
                .broadcast("binaryContents.updated", binaryContent);
    }
}
