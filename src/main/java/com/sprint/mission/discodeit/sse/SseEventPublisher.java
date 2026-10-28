package com.sprint.mission.discodeit.sse;

import java.util.Collection;
import java.util.UUID;

/**
 * 업무 이벤트를 지정 사용자 또는 전체 사용자에게 발행하는 서비스용 계약이다.
 * 호출자는 연결 목록을 직접 다루지 않고 이벤트 이름, DTO, 수신 대상만 결정한다.
 */
public interface SseEventPublisher {

    void send(Collection<UUID> receiverIds, String  eventName, Object data);

    void broadcast(String eventName, Object data);
}
