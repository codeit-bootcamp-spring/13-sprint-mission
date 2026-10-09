package com.sprint.mission.discodeit.event.binarycontent;


import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinaryContentEventListener {

    private final String SERVICE_NAME = "BinaryCreateEvent";

    private final BinaryContentStorage storage;
    private final BinaryContentService service;

    @Async("noty")
    @CacheEvict(cacheNames = CacheConfig.NOTIFICATION_CACHE, allEntries = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void create(BinaryContentCreatedEvent event){

        log.debug("[{}] - 바이너티 파일 생성 이벤트 호출 - id : {}",SERVICE_NAME, event.id());

        try {
            storage.put(event.id(), event.content());
            service.updateStatus(event.id(), BinaryContentStatus.SUCCESS);
            log.debug("[{}] - 데이터 저장 성공 - id : {}", SERVICE_NAME, event.id());
        } catch (RuntimeException e) {
            service.updateStatus(event.id(), BinaryContentStatus.FAIL);
            log.error("[{}] - 데이터 저장 실패 - id : {}", SERVICE_NAME, event.id(), e);
        }
    }

}
