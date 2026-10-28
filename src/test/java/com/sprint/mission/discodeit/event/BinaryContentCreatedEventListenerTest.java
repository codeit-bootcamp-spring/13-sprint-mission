package com.sprint.mission.discodeit.event;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
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
  @DisplayName("파일 저장에 성공하면 상태를 SUCCESS로 변경한다")
  void handle_Success() {
    UUID contentId = UUID.randomUUID();
    byte[] bytes = {1, 2, 3};

    BinaryContentCreatedEvent event =
        new BinaryContentCreatedEvent(contentId, bytes);

    listener.handle(event);

    var order = inOrder(binaryContentStorage, binaryContentService);
    order.verify(binaryContentStorage).put(contentId, bytes);
    order.verify(binaryContentService)
        .updateStatus(contentId, BinaryContentStatus.SUCCESS);

    then(binaryContentService).should(never())
        .updateStatus(contentId, BinaryContentStatus.FAIL);
  }

  @Test
  @DisplayName("파일 저장에 실패하면 상태를 FAIL로 변경한다")
  void handle_Fail() {
    UUID contentId = UUID.randomUUID();
    byte[] bytes = {1, 2, 3};

    BinaryContentCreatedEvent event =
        new BinaryContentCreatedEvent(contentId, bytes);

    given(binaryContentStorage.put(contentId, bytes))
        .willThrow(new IllegalStateException("파일 저장 실패"));

    listener.handle(event);

    then(binaryContentService).should()
        .updateStatus(contentId, BinaryContentStatus.FAIL);

    then(binaryContentService).should(never())
        .updateStatus(contentId, BinaryContentStatus.SUCCESS);
  }
}