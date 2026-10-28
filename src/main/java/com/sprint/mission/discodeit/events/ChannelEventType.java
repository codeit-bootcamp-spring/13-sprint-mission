package com.sprint.mission.discodeit.events;

import lombok.Getter;

/**
 * 클라이언트가 구독하는 채널 이벤트 이름과 서버의 변경 종류를 연결한다.
 */
@Getter
public enum ChannelEventType {
    CREATED("channels.created"),
    UPDATED("channels.updated"),
    DELETED("channels.deleted");

    private final String eventName;

    ChannelEventType(String eventName) {
        this.eventName = eventName;
    }
}
