package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.ChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelFindResponse;
import com.sprint.mission.discodeit.dto.response.ChannelUpdateResponse;
import com.sprint.mission.discodeit.dto.response.UserUpdateResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelService {

    Channel createChannel(ChannelCreateRequest channelCreateRequest);
    ChannelFindResponse findChannel(UUID channelId);
    List<ChannelFindResponse> findAllByUserId(UUID userId);
    ChannelUpdateResponse updateChannel(ChannelUpdateRequest channelUpdateRequest);
    void deleteChannel(UUID channelId);

}
