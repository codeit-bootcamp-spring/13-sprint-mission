package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;

import java.nio.file.*;
import java.util.*;

public interface ChannelService {

    ChannelResponse createPublicChannel(ChannelRequest.CreatePublicChannel publicChannel);

    ChannelResponse createPrivateChannel(ChannelRequest.CreatePrivateChannel privateChannel);

    ChannelResponse find(UUID id);

    List<ChannelResponse> findAll();

    ChannelResponse update(UUID id,ChannelRequest.UpdateChannel updateChannel);

    List<ChannelResponse> findAllByUserId(UUID userId);

    void delete(UUID id);

}
