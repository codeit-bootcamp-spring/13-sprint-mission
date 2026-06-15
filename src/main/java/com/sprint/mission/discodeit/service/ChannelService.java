package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.CreatePrivateChannelInput;
import com.sprint.mission.discodeit.dto.input.CreatePublicChannelInput;
import com.sprint.mission.discodeit.dto.output.ChannelOutput;


import java.util.List;
import java.util.UUID;

public interface ChannelService {
    void createPublicChannel(CreatePublicChannelInput cnp);
    void createPrivateChannel(CreatePrivateChannelInput cnp);
    ChannelOutput findChannelInfoById(UUID Channelid);
    List<ChannelOutput> findAllByUserID(UUID userID);
    void updateChannelInfo(UUID id, CreatePublicChannelInput cnp);
    void deleteChannel(UUID id);
}
