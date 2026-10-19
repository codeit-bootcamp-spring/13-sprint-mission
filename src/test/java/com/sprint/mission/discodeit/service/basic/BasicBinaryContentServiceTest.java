package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentRequest;
import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BasicBinaryContentServiceTest {

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private BinaryContentMapper binaryContentMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BasicBinaryContentService binaryContentService;

    @Test
    void create_메타데이터를_저장하고_생성_이벤트를_발행한다() {
        UUID binaryContentId = UUID.randomUUID();
        byte[] bytes = "test-content".getBytes();

        BinaryContentRequest request = new BinaryContentRequest(
                "test.txt",
                (long) bytes.length,
                "text/plain",
                bytes
        );

        BinaryContent savedBinaryContent = new BinaryContent(
                request.fileName(),
                request.size(),
                request.contentType()
        );
        ReflectionTestUtils.setField(
                savedBinaryContent,
                "id",
                binaryContentId
        );

        BinaryContentResponse expected = new BinaryContentResponse(
                binaryContentId,
                Instant.now(),
                request.fileName(),
                request.size(),
                request.contentType(),
                BinaryContentStatus.PROCESSING
        );

        given(binaryContentRepository.save(any(BinaryContent.class)))
                .willReturn(savedBinaryContent);
        given(binaryContentMapper.toDto(savedBinaryContent))
                .willReturn(expected);

        BinaryContentResponse actual =
                binaryContentService.create(request);

        ArgumentCaptor<BinaryContentCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        BinaryContentCreatedEvent.class
                );

        assertThat(actual).isEqualTo(expected);

        then(binaryContentRepository).should()
                .save(any(BinaryContent.class));
        then(eventPublisher).should()
                .publishEvent(eventCaptor.capture());

        BinaryContentCreatedEvent publishedEvent =
                eventCaptor.getValue();

        assertThat(publishedEvent.binaryContentId())
                .isEqualTo(binaryContentId);
        assertThat(publishedEvent.bytes())
                .isEqualTo(bytes);
    }

    @Test
    void updateStatus_바이너리_콘텐츠의_상태를_변경한다() {
        UUID binaryContentId = UUID.randomUUID();

        BinaryContent binaryContent = new BinaryContent(
                "test.txt",
                12L,
                "text/plain"
        );

        given(binaryContentRepository.findById(binaryContentId))
                .willReturn(Optional.of(binaryContent));

        binaryContentService.updateStatus(
                binaryContentId,
                BinaryContentStatus.SUCCESS
        );

        assertThat(binaryContent.getStatus())
                .isEqualTo(BinaryContentStatus.SUCCESS);
    }
}