package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    ChannelResponse createPublicChannel(PublicChannelCreateRequest request);
    ChannelResponse createPrivateChannel(PrivateChannelCreateRequest request);
    ChannelResponse find(UUID channelId);
    List<ChannelResponse> findAllByUserId(UUID userId);
    ChannelResponse update(UUID channelId, ChannelUpdateRequest request); // 식별자인 id가 먼저 오는 것이 흐름상 자연스럽다
    void delete(UUID channelId);
}
