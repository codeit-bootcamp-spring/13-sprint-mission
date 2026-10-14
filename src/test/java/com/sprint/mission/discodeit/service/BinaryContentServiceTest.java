package com.sprint.mission.discodeit.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class BinaryContentServiceTest {

  @Mock BinaryContentRepository repository;
  @Mock BinaryContentStorage binaryContentStorage;
  @Mock BinaryContentMapper binaryContentMapper;
  @Mock ApplicationEventPublisher eventPublisher;
  @InjectMocks BinaryContentService service;

  @Test
  void createPublishesSavedContentIdAndBytesWithoutCallingStorage() {
    byte[] bytes = {1, 2, 3};
    MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", bytes);
    BinaryContent saved = new BinaryContent("test.png", 3L, "image/png", null, null);
    when(repository.save(any(BinaryContent.class))).thenReturn(saved);

    service.create(new BinaryContentCreateRequest(file));

    ArgumentCaptor<BinaryContentCreatedEvent> captor =
        ArgumentCaptor.forClass(BinaryContentCreatedEvent.class);
    org.mockito.InOrder order = org.mockito.Mockito.inOrder(repository, eventPublisher);
    order.verify(repository).save(any(BinaryContent.class));
    order.verify(eventPublisher).publishEvent(captor.capture());
    assertEquals(saved.getId(), captor.getValue().binaryContentId());
    assertArrayEquals(bytes, captor.getValue().bytes());
    org.mockito.Mockito.verifyNoInteractions(binaryContentStorage);
  }
}
