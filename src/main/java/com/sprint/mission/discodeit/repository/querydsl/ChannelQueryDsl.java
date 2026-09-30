package com.sprint.mission.discodeit.repository.querydsl;

import com.sprint.mission.discodeit.dto.projection.ChannelProjection;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ChannelQueryDsl {
    /**
     * get channel and joined user ids.
     * 채널은 유저가 가입된 private 채널과, public 채널을 반환함.
     * @param id UUID
     * @return channelProjections
     */
    Map<UUID,ChannelProjection> getChannelsFromUserId(UUID id);

    /**
     * id 에 해당하는 채널의 정보와, 유저 목록, 마지막 메세지 시간.
     * @param id UUID
     * @return 채널 정보 및 유저 정보를 포함하는 ChannelProjection 객체
     */
    Optional<ChannelProjection> getChannelById(UUID id);



}
