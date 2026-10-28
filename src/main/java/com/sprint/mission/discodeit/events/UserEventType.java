package com.sprint.mission.discodeit.events;

import lombok.Getter;

/**
 * 사용자 생성·수정·삭제를 클라이언트가 처리하는 SSE 이벤트 이름으로 변환한다.
 */
@Getter
public enum UserEventType {
    CREATED("users.created"),
    UPDATED("users.updated"),
    DELETED("users.deleted");

    private final String eventName;

    UserEventType(String eventName) {
        this.eventName = eventName;
    }
}
