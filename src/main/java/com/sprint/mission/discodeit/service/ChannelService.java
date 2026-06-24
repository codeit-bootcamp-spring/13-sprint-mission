package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.CreatePrivateChannelRequest;
import com.sprint.mission.discodeit.dto.request.CreatePublicChannelRequest;
import com.sprint.mission.discodeit.dto.request.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelResponse;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    ChannelResponse createPublicChannel(CreatePublicChannelRequest request);

    ChannelResponse createPrivateChannel(CreatePrivateChannelRequest request);

    ChannelResponse find(UUID id);

    List<ChannelResponse> findAllByUserId(UUID userId);

    ChannelResponse update(UUID id, UpdateChannelRequest request);

    void delete(UUID id);

}
