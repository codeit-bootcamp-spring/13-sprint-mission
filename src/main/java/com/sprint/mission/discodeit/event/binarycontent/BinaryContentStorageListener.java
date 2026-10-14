package com.sprint.mission.discodeit.event.binarycontent;

import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class BinaryContentStorageListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCreated(BinaryContentCreatedEvent event) {
    try {
      binaryContentStorage.put(event.binaryContentId(), event.bytes());
    } catch (RuntimeException e) {
      binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.FAIL);
      log.error("바이너리 데이터 저장 실패: binaryContentId={}", event.binaryContentId(), e);
      return;
    }
    binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);
  }
}
