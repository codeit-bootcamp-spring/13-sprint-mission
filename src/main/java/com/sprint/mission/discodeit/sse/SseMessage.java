package com.sprint.mission.discodeit.sse;

import java.util.Set;
import java.util.UUID;

/**
 * 저장과 인스턴스 간 전달에 사용하는 불변 이벤트 스냅샷이다.
 * dataJson은 발행 시 직렬화한 본문이며 receiverIds도 방어적으로 복사한다.
 * broadcast=true이면 지정 수신자 목록과 관계없이 모든 사용자의 복원 대상이 된다.
 */
public record SseMessage(UUID eventId, String eventName, String dataJson, Set<UUID> receiverIds, boolean broadcast) {
    public SseMessage{
        receiverIds = Set.copyOf(receiverIds);
    }
}
