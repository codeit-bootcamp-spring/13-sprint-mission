package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelFindResponse;
import com.sprint.mission.discodeit.dto.response.ChannelUpdateResponse;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    Channel createPrivateChannel(PrivateChannelCreateRequest request);
    Channel createPublicChannel(PublicChannelCreateRequest request);
    ChannelFindResponse findChannel(UUID channelId);
    List<ChannelFindResponse> findAllByUserId(UUID userId);
    ChannelUpdateResponse updateChannel(ChannelUpdateRequest channelUpdateRequest);
    void deleteChannel(UUID channelId);

}
