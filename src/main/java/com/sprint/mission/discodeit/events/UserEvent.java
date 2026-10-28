package com.sprint.mission.discodeit.events;

import com.sprint.mission.discodeit.dto.response.UserDto;

/**
 * 사용자 정보 또는 인증 상태 변경 시점의 DTO와 변경 종류를 전달한다.
 * 삭제 역시 DTO 스냅샷을 사용하여 엔티티 수명과 이벤트 처리를 분리한다.
 */
public record UserEvent(UserDto user, UserEventType type) {

    public static UserEvent createEvent(UserDto user){
        return new UserEvent(user, UserEventType.CREATED);
    }

    public static UserEvent updateEvent(UserDto user){
        return new UserEvent(user, UserEventType.UPDATED);
    }

    public static UserEvent deleteEvent(UserDto user){
        return new UserEvent(user, UserEventType.DELETED);
    }

    public boolean isDeleteEvent(){
        return UserEventType.DELETED.equals(type);
    }
}
