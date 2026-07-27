package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;

import java.util.*;

public record UpdateChannelCommand(
        UUID channelId,
        String name,
        String description
) {

    public static UpdateChannelCommand from (UpdateChannelRequset request) {
        return new UpdateChannelCommand(request.channelId(), request.name(), request.description());
    }
}
