package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.entity.BinaryContent.Status;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinaryContentCreatedEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;

  @Async("uploadTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(BinaryContentCreatedEvent event) {
    try {
      binaryContentStorage.put(
          event.binaryContentId(),
          event.bytes()
      );

      binaryContentService.updateStatus(
          event.binaryContentId(),
          Status.SUCCESS
      );

      log.info("파일 저장 완료: binaryContentId={}", event.binaryContentId());

    } catch (Exception e) {
      binaryContentService.updateStatus(
          event.binaryContentId(),
          Status.FAIL
      );

      log.error("파일 저장 실패: binaryContentId={}", event.binaryContentId(), e);
    }
  }
}
