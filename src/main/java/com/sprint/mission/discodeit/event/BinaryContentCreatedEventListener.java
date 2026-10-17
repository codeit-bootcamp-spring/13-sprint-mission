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

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinaryContentCreatedEventListener {

    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentService binaryContentService;

    @Async("taskExecutor")
    @TransactionalEventListener (phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BinaryContentCreatedEvent event) {
        try {
            binaryContentStorage.put(
                    event.binaryContentId(),
                    event.bytes()
            );
        } catch (RuntimeException e) {
            log.error("바이너리 데이터 저장 실패. id={}", event.binaryContentId(), e);

            updateStatusToFail(event.binaryContentId());
            return;
        }

        binaryContentService.updateStatus(
                event.binaryContentId(),
                BinaryContentStatus.SUCCESS
        );

        log.info("바이너리 데이터 저장 성공. id={}", event.binaryContentId());
    }

    private void updateStatusToFail(UUID binaryContentId) {
        try {
            binaryContentService.updateStatus(binaryContentId,
                    BinaryContentStatus.FAIL);
        }catch (RuntimeException e) {
            log.error("바이너리 콘텐츠 실패 상태 반영 중 오류 발생. id={}", binaryContentId, e);
        }
    }




}
