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

@Component
@RequiredArgsConstructor
@Slf4j
public class BinaryContentCreatedEventListener {

    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentService binaryContentService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBinaryContentCreated(BinaryContentCreatedEvent event) {
        try {
            binaryContentStorage.put(event.binaryContentId(), event.bytes());

            binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);
        } catch (Exception e) {
            log.error("BinaryContent 업로드 실패. binaryContentId={}", event.binaryContentId(), e);
            binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.FAIL);
        }

    }

}
