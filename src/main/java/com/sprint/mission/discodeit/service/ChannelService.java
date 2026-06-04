package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.ChannelProfile;
import com.sprint.mission.discodeit.dto.input.CreateReadyStatusInput;
import com.sprint.mission.discodeit.dto.output.ChannelOutput;


import java.util.List;
import java.util.UUID;

public interface ChannelService {
    void createPublicChannel(ChannelProfile cnp);
    void createPrivateChannel(CreateReadyStatusInput rsi);
    ChannelOutput findChannelInfoById(UUID Channelid);
    List<ChannelOutput> findAllByUserID(UUID userID);
    void updateChannelInfo(UUID id, ChannelProfile cnp);
    void deleteChannel(UUID id);
}
