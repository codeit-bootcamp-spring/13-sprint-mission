package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;

import java.util.*;

public record UpdateChannelCommand(
        String name,
        String description
) {

    public static UpdateChannelCommand from (UpdateChannelRequset request) {
        return new UpdateChannelCommand(request.name(), request.description());
    }
}
