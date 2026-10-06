package com.sprint.mission.discodeit.event.binarycontent;


import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BinaryContentEventListener {

    private final BinaryContentStorage storage;
    private final BinaryContentService service;

    @Async("noty")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void create(BinaryContentCreatedEvent event){
        try {
            storage.put(event.id(), event.content());
            service.updateStatus(event.id(), BinaryContentStatus.SUCCESS);
        } catch (RuntimeException e) {
            service.updateStatus(event.id(), BinaryContentStatus.FAIL);
        }
    }

}
