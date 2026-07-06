package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;


import java.util.List;
import java.util.UUID;

public interface ChannelService {
    ChannelDto createPublicChannel(PublicChannelCreateRequest cnp);
    ChannelDto createPrivateChannel(PrivateChannelCreateRequest cnp);
    List<ChannelDto> findAllByUserID(UUID userID);
    ChannelDto update(UUID id, PublicChannelUpdateRequest uci);
    void deleteChannel(UUID id);
}
