package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.events.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentStatusUpdater;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 파일 업로드 결과를 별도 트랜잭션으로 반영하고 변경된 DTO 이벤트를 발행한다.
 * 외부 업로드 처리가 끝난 뒤 상태를 독립적으로 저장하며 SSE 리스너는 커밋 후 전송한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class TransactionalBinaryContentStatusUpdater implements BinaryContentStatusUpdater {

    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentMapper binaryContentMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 대상 파일이 존재하면 상태를 변경하고 변경 후 DTO를 이벤트로 발행한다.
     * 파일이 삭제되어 없으면 상태 변경과 이벤트 발행을 생략한다.
     */
    @Override
    public void updateStatus(UUID binaryContentId, BinaryContentStatus targetStatus) {
        binaryContentRepository.findById(binaryContentId)
                .ifPresentOrElse(
                        content -> updateBinaryContentStatus(targetStatus, content),
                        () -> log.warn("메타데이터가 없어 업로드 상태를 변경할 수 없습니다. binaryContentId={}, targetStatus={}",
                                binaryContentId, targetStatus)
                );
    }

    private void updateBinaryContentStatus(BinaryContentStatus targetStatus, BinaryContent content) {
        BinaryContentStatus previousStatus = content.getStatus();
        content.updateStatus(targetStatus);
        eventPublisher.publishEvent(new BinaryContentUpdatedEvent(binaryContentMapper.toDto(content)));
        log.debug("바이너리 파일 업로드 상태를 변경했습니다. binaryContentId={}, previousStatus={}, targetStatus={}",
                content.getId(), previousStatus, targetStatus);

    }
}
