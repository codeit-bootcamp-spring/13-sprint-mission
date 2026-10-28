package com.sprint.mission.discodeit.events;

import com.sprint.mission.discodeit.dto.response.ChannelDto;

/**
 * 채널 생성·수정·삭제 종류와 해당 시점의 DTO를 전달한다.
 * 삭제 이벤트도 삭제 전 참여자 정보를 보관하여 커밋 후 수신 대상을 결정할 수 있다.
 */
public record ChannelEvent(ChannelDto channel, ChannelEventType type) {

    public static ChannelEvent createEvent(ChannelDto channel){
        return new ChannelEvent(channel, ChannelEventType.CREATED);
    }

    public static ChannelEvent updateEvent(ChannelDto channel){
        return new ChannelEvent(channel, ChannelEventType.UPDATED);
    }

    public static ChannelEvent deleteEvent(ChannelDto channel){
        return new ChannelEvent(channel, ChannelEventType.DELETED);
    }
}
