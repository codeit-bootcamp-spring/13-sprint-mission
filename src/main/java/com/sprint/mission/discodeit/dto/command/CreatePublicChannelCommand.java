package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;

public record CreatePublicChannelCommand(
        String name,
        String description
) {

    public CreatePublicChannelCommand from(CreatePublicChannelRequest request) {
        return new CreatePublicChannelCommand(request.name(), request.description());
    }
}
