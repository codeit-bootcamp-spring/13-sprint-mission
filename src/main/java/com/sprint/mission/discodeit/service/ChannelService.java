package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    //PUBLIC 채널 생성
    ChannelResponse createPublicChannel(PublicChannelRequest request);
    //PRIVATE 채널 생성
    ChannelResponse createPrivateChannel(PrivateChannelRequest request);

    //채널단건 조회
    ChannelResponse findByChannel(UUID channelId);

    //사욪자 전체 채널 조회
    List<ChannelResponse> findAllByUserId(UUID id);

    //수정
    ChannelResponse updateChannel(UUID channelId, ChannelUpdateRequest request);

    //사제
    void deleteChannel(UUID channelId);
}
