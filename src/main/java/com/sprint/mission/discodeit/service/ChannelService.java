package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.dto.request.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.dto.request.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    Channel createPublicChannel(CreatePublicChannelRequest request);

    Channel createPrivateChannel(CreatePrivateChannelRequest request);

    ChannelResponse find(UUID id);

    List<ChannelResponse> findAllByUserId(UUID userId);

    void update(UpdateChannelRequest request);

    void delete(UUID id);

}
