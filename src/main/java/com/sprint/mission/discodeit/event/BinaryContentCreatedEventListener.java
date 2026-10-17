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
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(BinaryContentCreatedEvent event) {
        try {
            binaryContentStorage.put(
                    event.binaryContentId(),
                    event.bytes()
            );

            binaryContentService.updateStatus(
                    event.binaryContentId(),
                    BinaryContentStatus.SUCCESS
            );

            log.info(
                    "파일 업로드 완료: binaryContentId={}",
                    event.binaryContentId()
            );
        } catch (RuntimeException exception) {
            binaryContentService.updateStatus(
                    event.binaryContentId(),
                    BinaryContentStatus.FAIL
            );

            log.error(
                    "파일 업로드 실패: binaryContentId={}",
                    event.binaryContentId(),
                    exception
            );
        }
    }
}