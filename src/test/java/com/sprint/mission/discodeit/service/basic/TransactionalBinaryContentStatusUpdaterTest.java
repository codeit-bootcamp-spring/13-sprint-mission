package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.events.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 실제 상태 변경 서비스를 실행하고 실제 BinaryContent의 변경 결과와 이벤트 DTO를 확인한다.
 * Repository·Mapper·이벤트 발행자는 Mock이므로 REQUIRES_NEW 커밋 자체를 검증하는 테스트는 아니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("바이너리 파일 상태 변경 서비스 단위 테스트")
class TransactionalBinaryContentStatusUpdaterTest {
    @Mock BinaryContentRepository repository;
    @Mock BinaryContentMapper binaryContentMapper;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks TransactionalBinaryContentStatusUpdater updater;

    @ParameterizedTest
    @EnumSource(value = BinaryContentStatus.class, names = {"SUCCESS", "FAIL"})
    @DisplayName("업로드 결과를 반영하고 변경된 파일 DTO로 이벤트를 발행한다")
    void updateStatus_changesEntityStateAndPublishesEvent(BinaryContentStatus targetStatus) {
        UUID id = UUID.randomUUID();
        BinaryContent content = BinaryContent.createProcessing("file.txt", "text/plain", 1L);
        ReflectionTestUtils.setField(content, "id", id);

        BinaryContentDto dto = new BinaryContentDto(
                id, "file.txt", 1L, "text/plain", targetStatus
        );

        given(repository.findById(id)).willReturn(Optional.of(content));
        given(binaryContentMapper.toDto(content)).willAnswer(invocation -> {
            // DTO 변환 시점에 이미 상태가 변경됐는지 확인한다.
            assertThat(content.getStatus()).isEqualTo(targetStatus);
            return dto;
        });

        updater.updateStatus(id, targetStatus);

        assertThat(content.getStatus()).isEqualTo(targetStatus);
        verify(eventPublisher).publishEvent(new BinaryContentUpdatedEvent(dto));
    }

    @Test
    @DisplayName("메타데이터가 없으면 새로 저장하지 않고 상태 변경을 생략한다")
    void updateStatus_skipsMissingMetadata() {
        UUID id = UUID.randomUUID();
        given(repository.findById(id)).willReturn(Optional.empty());

        assertThatCode(() -> updater.updateStatus(id, BinaryContentStatus.FAIL)).doesNotThrowAnyException();

        verify(repository).findById(id);
        verify(repository, never()).save(any());
        verifyNoInteractions(binaryContentMapper, eventPublisher);
    }
}
