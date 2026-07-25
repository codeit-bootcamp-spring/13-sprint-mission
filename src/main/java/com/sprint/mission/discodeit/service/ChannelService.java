package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.ChannelFindResponse;
import com.sprint.mission.discodeit.dto.response.ChannelUpdateResponse;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    ChannelDto createPrivateChannel(PrivateChannelCreateRequest request);
    ChannelDto createPublicChannel(PublicChannelCreateRequest request);
    ChannelDto getChannel(UUID channelId);
    List<ChannelDto> getChannelsByUserId(UUID userId);
    ChannelDto updateChannel(UUID channelId, PublicChannelUpdateRequest publicChannelUpdateRequest);
    void deleteChannel(UUID channelId);

}
