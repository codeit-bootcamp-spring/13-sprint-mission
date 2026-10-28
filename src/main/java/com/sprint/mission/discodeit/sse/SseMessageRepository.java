package com.sprint.mission.discodeit.sse;

import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;

/**
 * SSE 이력 저장·발행과 재접속 커서 조회를 위한 공통 계약이다.
 * 구현체는 저장 순번을 부여한 뒤 이벤트를 발행하며, 동일 eventId는 다시 저장하지 않는다.
 * 조회 결과에는 전체 방송 또는 해당 사용자에게 지정된 이벤트만 포함한다.
 */
public interface SseMessageRepository {

    boolean saveAndPublish(SseMessage message);

    OptionalLong findSequence(UUID eventId);

    List<SseMessage> findAllAfter(UUID receiverId, UUID lastEventId);
}
