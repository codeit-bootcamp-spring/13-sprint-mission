package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.input.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.input.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.output.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;


import java.util.List;
import java.util.UUID;

public interface ChannelService {
    Channel createPublicChannel(PublicChannelCreateRequest cnp);
    Channel createPrivateChannel(PrivateChannelCreateRequest cnp);
    List<ChannelDto> findAllByUserID(UUID userID);
    Channel update(UUID id, PublicChannelUpdateRequest uci);
    void deleteChannel(UUID id);
}
