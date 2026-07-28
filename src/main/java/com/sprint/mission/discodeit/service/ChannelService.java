package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.response.*;

import java.util.*;

public interface ChannelService {

    ChannelDto createPublicChannel(CreatePublicChannelCommand command);

    ChannelDto createPrivateChannel(CreatePrivateChannelCommand command);

    ChannelDto find(UUID id);

    ChannelDto update(UUID id, UpdateChannelCommand command);

    List<ChannelDto> findAllByUserId(UUID userId);

    void delete(UUID id);

}
