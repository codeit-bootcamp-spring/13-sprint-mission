package com.sprint.mission.discodeit.websocket;

import com.sprint.mission.discodeit.dto.response.MessageDto;

/**
 * 생성된 채팅 DTO를 WebSocket 전달 경로로 넘기는 공통 계약이다.
 * 인메모리는 로컬 브로커로, Redis 모드는 전체 인스턴스의 구독자로 전달한다.
 */
public interface WebSocketMessagePublisher {

    void publish(MessageDto message);
}
