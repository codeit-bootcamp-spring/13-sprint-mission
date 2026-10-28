package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentCreatedEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;

  @Async("fileUploadExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(BinaryContentCreatedEvent event) {
    try {
      binaryContentStorage.put(event.binaryContentId(), event.bytes());
    } catch (Exception e) {
      log.error("파일 저장 실패 - binaryContentId: {}", event.binaryContentId(), e);
      binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.FAIL);
      return;
    }

    binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);
  }
}